(ns capashity.report
  (:require [hiccup.core :refer :all]
            [hiccup.page :refer :all]))

;;(defn d []
;;  (capashity.core/sum-up (deref (:result/histories capashity.core/system))))

(defn extract-column-labels [data]
  (->> data
       first
       :counts
       (map :name)))

(html [:table.table])

(defn tablize [data]
  (let [columns (extract-column-labels data)]
    [:table.table
     [:tr
      [:th]
      (for [col columns]
        [:th col])]
     (for [row data]
       [:tr
        [:th (:event row)]
        (for [cnt (:counts row)]
          [:td (:count cnt)])])]))

(defn publish-html [data]
  (html5 {:lang "ja"}
         [:head
          [:title "report"]
          [:meta {:charset "utf-8"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
          [:link {:rel "stylesheet" :href "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/css/bootstrap.min.css" :integrity "sha384-PsH8R72JQ3SOdhVi3uxftmaW6Vc51MKb0q5P2rRUpPvrszuE4W1povHYgTpBfshb" :crossorigin "anonymous"}]]
         [:body (tablize data)
          [:script {:src "https://code.jquery.com/jquery-3.2.1.slim.min.js" :integrity "sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" :crossorigin "anonymous"}]
          [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.3/umd/popper.min.js" :integrity "sha384-vFJXuSJphROIrBnz7yo7oB41mKfc8JzQZiCq4NCceLEaO4IHwicKwpJf9c9IpFgh" :crossorigin "anonymous"}]
          [:script {:src "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/js/bootstrap.min.js" :integrity "sha384-alpBpkh1PFOepccYVYDB4do5UnbKysX5WZXm3XxPqe5iKTfUKjNkCk9SaVuEZflJ" :crossorigin "anonymous"}]]))
