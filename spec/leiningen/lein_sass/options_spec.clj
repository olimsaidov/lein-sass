(ns leiningen.lein-sass.options-spec
  (:use [speclj.core]
        [leiningen.lein-sass.options]))

(describe "options"

  (describe "fn extract-option"

    (it "returns nil when  it doesn't find the src-type in the project map"
      (with-out-str ;; Just to capture the println output
        (should= nil (extract-options :sass {}))))

    (it "warns the user when it doesn't find the src-type in the project map"
      (should= "WARNING: no :sass entry found in project definition.\n"
        (with-out-str (extract-options :sass {}))))

    (it "doesn't warns the user when it finds the src-type in the project map"
      (def have-called-println (atom false))
      (with-redefs [println (fn [& messages]
                              (reset! have-called-println true))]
        (should-not @have-called-println)
        (extract-options :sass {:sass {}})
        (should-not @have-called-println)))

    (it "references the correct gem name for sass src-type"
      (should= "sass" (:gem-name (extract-options :sass {:sass {}}))))

    (it "references the correct gem name for scss src-type"
      (should= "sass" (:gem-name (extract-options :scss {:scss {}}))))

    (it "ignores the clean hook"
      (should= #{:clean}
        (:ignore-hooks (extract-options :sass {:sass {:ignore-hooks [:clean]}}))))

    (it "ignores the compile hook"
      (should= #{:once}
        (:ignore-hooks (extract-options :sass {:sass {:ignore-hooks [:compile]}}))))

    (it "contains the source type"
      (should= :sass
        (:src-type (extract-options :sass {:sass {}}))))

    (context "defaults"
      (it "uses the 'resources' folder"
        (should= "resources"
          (:src (extract-options :sass {:sass {}}))))

      (it "doesn't set any default extension"
        (should= "css"
          (:output-extension (extract-options :sass {:sass {}}))))

      (it "deletes the output directory"
        (should (:delete-output-dir (extract-options :sass {:sass {}}))))

      (it "uses a compile delay of 250 ms"
        (should= 250
          (:auto-compile-delay (extract-options :sass {:sass {}}))))

      (it "contains a :nested formatting style for sass"
        (should= :nested (:style (extract-options :sass {:sass {}}))))

      (it "contains a :nested formatting style for scss"
        (should= :nested (:style (extract-options :scss {:scss {}})))))

    (context "overwriting defaults"
      (it "lets you set sources folder"
        (should= "other/folder"
          (:src (extract-options :sass {:sass {:src "other/folder"}}))))

      (it "lets you set the output extension"
        (should= "ext"
          (:output-extension (extract-options :sass {:sass {:output-extension "ext"}}))))

      (it "lets you unset the delete outpout directory flag"
        (should-not (:delete-output-dir (extract-options :sass {:sass {:delete-output-dir false}}))))

      (it "lets you set a compile delay"
        (should= 500
          (:auto-compile-delay (extract-options :sass {:sass {:auto-compile-delay 500}}))))

      (it "lets you set the formatting style for sass"
        (should= :compressed (:style (extract-options :sass {:sass {:style :compressed}}))))

      (it "lets you set the formatting style for scss"
        (should= :compressed (:style (extract-options :scss {:scss {:style :compressed}})))))))
