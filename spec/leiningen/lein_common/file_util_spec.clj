(ns leiningen.lein-common.file-util-spec
  (:use [speclj.core]
        [leiningen.lein-common.file-utils])
  (:require [clojure.java.io :as io]))

(describe "file-util"

  (describe "fn dir-empty?"
    (it "is true when the directory is empty"
      (should (dir-empty? "spec/files/empty_dir")))

    (it "is false when the directory not is empty"
      (should-not (dir-empty? "spec/files"))))

  (describe "fn delete-directory-recursively!"
    (it "doesn't do anything if the file doesn't exists"
      (should-not (.exists (io/file "spec/file/does_not_exits")))
      (with-out-str
        (should-not-throw (delete-directory-recursively! "spec/file/does_not_exits")))
      (should-not (.exists (io/file "spec/file/does_not_exits"))))

    (it "deletes the directory recursively"
      (io/make-parents "spec/out/sub/blah")
      (spit "spec/out/blah" "blah")
      (spit "spec/out/sub/blah" "blah")
      (with-out-str
        (delete-directory-recursively! "spec/out"))
      (should-not (.exists (io/file "spec/out"))))))
