(ns leiningen.render
  (:use leiningen.utils)
  (:require [clojure-watch.core :refer [start-watch]]
            [clojure.java.shell :as shell]
            [clojure.java.io :as io]))

(defn render
  [src-file dest-file options]
  (when (not (is-partial? src-file))
    (io/make-parents dest-file)
    (println (str "  [sass] - " (.getName src-file)))
    (shell/sh "sassc" "-t" (name (:style options)) (.getPath src-file) (.getPath dest-file))))

(defn render-once!
  [options]
  (let [descriptors (files-from options)]
    (doseq [[src-file dest-file] descriptors]
      (render src-file dest-file options))))

(defn render-loop!
  ([options]
    (render-once! options)
    (start-watch [{:path (:src options)
                   :event-types [:create :modify :delete]
                   :callback (fn [_ _] (render-once! options))
                   :options {:recursive true}}])
    (loop []
      (recur))))