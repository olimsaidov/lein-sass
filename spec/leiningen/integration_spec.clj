(ns leiningen.integration-spec
  (:use [speclj.core]
        [clojure.java.shell :only [sh]])
  (:require [leiningen.lein-common.file-utils :as futils]
            [clojure.java.io :as io]))

(describe "integration tests on tasks"
  ;; This is not ideal but provides some way of testing the tasks (given
  ;; that I have figured out how to include leiningen dependencies in
  ;; the tests) especially: we are relying on the project.clj file
  ;; (which can't be changed from here)

  (with-all ends-with-extension #'futils/ends-with-extension)
  (before (with-out-str (futils/delete-directory-recursively! "spec/out")))

  (defn run-plugin [task task-arg] (sh "lein" "with-profile" "plugin-example" task task-arg))

  (describe "sass")
  (describe "scss")
  )