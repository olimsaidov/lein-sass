(ns leiningen.render
  (:use leiningen.utils)
  (:require [clojure-watch.core :refer [start-watch]]
            [clojure.java.shell :as shell]
            [clojure.java.io :as io])
  (:import java.lang.Thread))

(defn render
  [src-file dest-file {:keys [style source-maps]}]
  (when (not (is-partial? src-file))
    (io/make-parents dest-file)
    (let [src-path (.getPath src-file)
          dest-path (.getPath dest-file)
          sass-style (name style)
          opts [ "-t" sass-style src-path dest-path]
          add-opts (if source-maps ["-m"] [])]
      (println (str "  [sass] - " (.getName src-file)))
      (println (:err (apply shell/sh (concat ["sassc"] add-opts opts)))))))

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
    (let [t (Thread/currentThread)]
      (locking t
        (.wait t)))))
