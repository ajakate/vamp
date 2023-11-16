(ns vamp.app
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [vamp.events]
            [vamp.music :as m]
            [re-frame.core :as rf]))

(defn button [content]
  (let [selected-chords @(rf/subscribe [:selected-chords])
        color-class (if (contains? selected-chords content) "bg-cyan-400" "bg-cyan-100")]
    [:button.p-3.rounded-md
     {:on-click #(rf/dispatch [:toggle-chord content])
      :class color-class}
     content]))

(defn chord-grid []
  [:div
   [:div.grid.grid-rows-3.grid-flow-col.gap-3
    (for [chord m/chords]
      ^{:key chord}
      [button chord])]
   [:div.flex.flex-row-reverse.gap-4.my-3
    [:button.p-3.rounded-md.bg-blue-100
     {:on-click #(rf/dispatch [:clear-selected])}
     "Clear All"]
    [:button.p-3.rounded-md.bg-blue-400
     {:on-click #(rf/dispatch [:select-all])}
     "Select All"]]])

(defn practice-section []
  (let [active-chord @(rf/subscribe [:active-chord])]
    [:div
     [:div.text-6xl.mx-auto.my-14 active-chord]
     [:button.p-3.rounded-md.bg-lime-400
      {:on-click #(rf/dispatch [:cycle-active-chord])}
      "Next"]
     [:div [:input {:type "range" }]]]))

(defn vamp-section []
  (let [vamp-chords @(rf/subscribe [:active-vamp])]
    [:div
     [:div.my-auto.mx-auto.text-4xl.py-14
      (for [chord vamp-chords]
        ^{:key chord}
        [:span.mx-4 chord])]
     [:button.p-3.rounded-md.bg-lime-400
      {:on-click #(rf/dispatch [:cycle-active-vamp])}
      "Next"]]))

(defn main-page []
  [:div.max-w-5xl.m-auto
   [:div.grid.grid-cols-2
    [practice-section]
    [vamp-section]]
   [:div.my-24]
   [chord-grid]])

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (rdom/render [main-page] (js/document.getElementById "root")))

(defn ^:export ^:dev/once init []
  (rf/dispatch-sync [:init-local-storage])
  (rf/dispatch-sync [:cycle-active-chord])
  (rf/dispatch-sync [:cycle-active-vamp])
  (mount-root))
