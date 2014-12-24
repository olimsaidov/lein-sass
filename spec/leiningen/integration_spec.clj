(ns leiningen.integration-spec
  (:use [speclj.core]
        [clojure.java.shell :only [sh]])
  (:require [leiningen.lein-common.file-utils :as futils]
            [clojure.java.io :as io]))

(describe "integration tests on tasks"
  ;; This is not ideal but provides some way of testing the tasks (given
  ;; that I have figured out how to include leiningen dependencoes in
  ;; the tests) especially: we are relying on the project.clj file
  ;; (which can't be changed from here)

  (with-all ends-with-extension #'futils/ends-with-extension)
  (before (with-out-str (futils/delete-directory-recursively! "spec/out")))

  (defn run-plugin [task task-arg] (sh "lein" "with-profile" "plugin-example" task task-arg))

  (describe "scss"
    (def scss (partial run-plugin "scss"))

    (context "once"
      (it "compiles the files in the correct directory"
        (scss "once")

        (let [all-files (file-seq (io/file "spec/out"))
              css-files (filter #(@ends-with-extension % "css") all-files)]
          (should= 4 (count all-files))
          (should= 2 (count css-files)))

        (let [file-content (slurp "spec/out/scss/foo.css")
              expected-content ".wide {\n  width: 100%; }\n\n.foo {\n  display: block; }\n"]
          (should= expected-content file-content))

        (let [file-content (slurp "spec/out/scss/bar.css")
              expected-content ".bar {\n  display: none; }\n"]
          (should= expected-content file-content))))

    (context "auto")

    (context "clean"
      (it "removes all artifacts that were created by scss task"
        (scss "once")
        (should (.exists (io/file "spec/out/scss")))

        (scss "clean")
        (should-not (.exists (io/file "spec/out/scss"))))

      (it "only deletes the artifacts that were created by scss task"
        (scss "once")
        (should (.exists (io/file "spec/out/scss")))

        (spit "spec/out/scss/not-generated" "a non generated content")

        (scss "clean")
        (should (.exists (io/file "spec/out/scss/not-generated")))
        (should-not (.exists (io/file "spec/out/scss/multiple/blah.html")))
        (should-not (.exists (io/file "spec/out/scss/multiple/blah2.html")))
        (should-not (.exists (io/file "spec/out/scss/single/haml/blah.html"))))))

  (describe "sass"))