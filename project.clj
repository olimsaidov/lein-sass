(defproject lein-sass "0.3.0-SNAPSHOT"
  :description "SASS autobuilder plugin"
  :url "https://github.com/101loops/lein-sass"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.jruby/jruby-complete "1.7.18"]
                 [com.cemerick/pomegranate "0.2.0"]
                 [clojure-watch "LATEST"]
                 [me.raynes/fs "1.4.6"]]

  :profiles {:dev {:dependencies [[speclj "2.5.0"]
                                  [org.rubygems/sass "3.3.0.rc.2"]]
                   :plugins [[speclj "2.5.0"]]
                   :test-paths ["spec/"]
                   :repositories [["gem-jars" "http://deux.gemjars.org"]]}

             :plugin-example {
                              ;; Example for adding lein hooks
                              ;; :hooks [leiningen.sass]

                              ;; Example on how to use lein-sass

                              :sass {:src "spec/files"
                                     :output-directory "spec/out"
                                     ;; Other options (provided are default values)
                                     ;; :delete-output-dir true ;; -> when running lein clean it will delete the output directory if it does not contain any file
                                     :style :nested ;; valid: :nested, :expanded, :compact, :compressed
                                     }
                              }
             }

  :eval-in-leiningen true
  :min-lein-version "2.0.0"
  )
