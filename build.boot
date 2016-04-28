(set-env!
  :source-paths #{"src/cljs"}
  :resource-paths #{"html" "sass"}
  :dependencies '[[org.clojure/clojure "1.8.0"]
                  [org.clojure/clojurescript "1.8.51"]
                  [boot/core "2.5.1" :scope "provided"]
                  [tolitius/boot-check "0.1.1"]
                  [adzerk/boot-cljs "1.7.170-3"]
                  [pandeiro/boot-http "0.7.0"]
                  [adzerk/boot-reload "0.4.2"]
                  [adzerk/boot-cljs-repl "0.3.0"]
                  [mathias/boot-sassc "0.1.5"]
                  [reagent "0.6.0-alpha"]
                  [com.cemerick/piggieback "0.2.1" :scope "test"]
                  [weasel "0.7.0" :scope "test"]
                  [org.clojure/tools.nrepl "0.2.12" :scope "test"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[mathias.boot-sassc :refer [sass]]
         '[tolitius.boot-check :as check])

(deftask
  tidy
  "Make sure your code is squeeky clean"
  []
  (comp
    (check/with-kibit)
    (check/with-yagni)
    (check/with-bikeshed)))

(deftask
  dev
  "Launch immediate feedback dev environment"
  []
  (comp
    (serve :dir "target")
    (watch)
    (sass)
    (reload)
    (cljs-repl)
    (cljs)
    (target :dir #{"target"})))
