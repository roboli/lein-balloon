(defproject org.clojars.roboli/lein-balloon "0.1.2"
  :description "Balloon plugin for Leiningen"
  :url "https://github.com/roboli/lein-balloon"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/tools.cli "1.0.219"]
                 [org.clojars.roboli/balloon "0.2.1"]
                 [org.clojars.mitch-kyle/clipboard "1.1.0"]
                 [cheshire "5.12.0"]]
  :eval-in-leiningen true)
