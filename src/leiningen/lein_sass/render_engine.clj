(ns leiningen.lein-sass.render-engine
  (:use leiningen.lein-common.file-utils)
  (:require [clojure.java.io :as io]
            [clojure-watch.core :refer [start-watch]])
  (:import [org.jruby.embed ScriptingContainer LocalContextScope]
           [org.jruby RubyHash RubySymbol RubyArray]
           (java.util Date)))

(def ^:private c (ref nil))
(def ^:private runtime (ref nil))

(def ^:private rendering-engine (ref nil))
(def ^:private rendering-options (ref nil))

(defn- rb-array [coll]
  (let [array (RubyArray/newArray @runtime)]
    (doseq [v coll] (.add array v))
    array))

(defn- rb-symbol [string]
  (RubySymbol/newSymbol @runtime (name string)))

(defn- rb-options [options]
  (let [rb-hash (RubyHash. @runtime)]
    (doseq [[k v] options]
      (let [key (rb-symbol k)
            value (if (coll? v) (rb-array v) (rb-symbol v))]
        (.put rb-hash key value)))
    rb-hash))

(defn- build-sass-options [{:keys [src src-type output-directory style]}]
  (rb-options {:syntax src-type
               :style (or style :nested)
               :load_paths [src output-directory]}))

(defn- require-gem [gem-name]
  (.runScriptlet @c (str "require 'rubygems'; require '" (name gem-name) "';")))

;; TODO improve this function (this is messy)
(defn- ensure-engine-started! [options]
  (when-not @c
    (dosync
      (ref-set c (ScriptingContainer. LocalContextScope/THREADSAFE))

      (require-gem (:gem-name options))
      (ref-set runtime (-> (.getProvider @c) .getRuntime))

      (do
        (ref-set rendering-engine (.runScriptlet @c "Sass::Engine"))
        (ref-set rendering-options (build-sass-options options))))))

(defn- is-partial? [file]
  (.startsWith (.getName file) "_"))

(defn- source-file-filter [src-type]
  #(let [f %
         extension-filter (extension-filter (name src-type))]
    (and (extension-filter f) (not (is-partial? f)))))

(defn- files-from [{:keys [src src-type output-directory output-extension]}]
  (dest-files-from (source-file-filter src-type) (name src-type) src output-directory output-extension))

(defn render [template]
  (try
    (let [args (to-array [template @rendering-options])
          engine (.callMethod @c @rendering-engine "new" args Object)]
      (.callMethod @c engine "render" String))
    (catch Exception e
      ;; ruby gem will print an error message
      (println "      -> Compilation failed\n\n"))))

(defn- rebuild-file?
  "Given a file descriptor, return true if that file needs to be rebuilt"
  [src-type file-descriptor]
  (let [dest-file (io/file (:dest file-descriptor))
        src-file (io/file (src-type file-descriptor))]
    (or (not (.exists dest-file))
      (> (.lastModified src-file) (.lastModified dest-file)))))

(defn render-once!
  [{:keys [src-type] :as options} force?]
  (ensure-engine-started! options)
  (let [descriptors (files-from options)
        building-any? (some #(rebuild-file? src-type %) descriptors)]
    (doseq [file-descriptor descriptors]
      (when (or force? building-any?)
        (let [dest-file (io/file (:dest file-descriptor))
              src-file (io/file (src-type file-descriptor))]
          (println (str "   [" (name src-type) "] - " (Date.) " - " src-file " -> " dest-file))
          (io/make-parents dest-file)
          (spit dest-file (render (slurp (src-type file-descriptor)))))))))

(defn render-loop!
  ([options force?]
    (render-once! options force?)
    (start-watch [{:path (:src options)
                   :event-types [:create :modify :delete]
                   :bootstrap (fn [path])
                   :callback (fn [event filename]
                               (println event filename)
                               (render-once! options false))
                   :options {:recursive true}}])
    (loop []
      (recur))))

(defn clean-all! [{:keys [output-directory delete-output-dir] :as options}]
  (doseq [file-descriptor (files-from options)]
    (delete-file! (io/file (:dest file-descriptor))))

  (when (and delete-output-dir (exists output-directory) (dir-empty? output-directory))
    (println (str "Destination folder " output-directory " is empty - Deleting it"))
    (delete-directory-recursively! output-directory)))
