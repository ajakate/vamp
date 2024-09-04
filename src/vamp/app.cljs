(ns vamp.app
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [vamp.events]
            [vamp.music :as m]
            [re-frame.core :as rf]))

(defn chord-slider []
  (let [chord-type @(rf/subscribe [:chord-type])
        jazz-color-class (if (= chord-type "jazz") "bg-indigo-400" "bg-indigo-50")
        triad-color-class (if (= chord-type "triad") "bg-indigo-400" "bg-indigo-50")]
    [:div.my-5.flex
     [:button.p-3.rounded-l-lg.max-w-32
      {:on-click #(rf/dispatch [:set-chord-type "jazz"])
       :class jazz-color-class}
      "Jazz Chords"]
     [:button.p-3.rounded-r-lg.max-w-32
      {:on-click #(rf/dispatch [:set-chord-type "triad"])
       :class triad-color-class}
      "Triad Chords"]]))

(defn chord-button [content]
  (let [selected-chords @(rf/subscribe [:selected-chords])
        chord-count (get selected-chords content)
        chord-count (if (nil? chord-count) 0 chord-count)
        color-class (if (< 0 chord-count) "bg-cyan-400" "bg-cyan-100")]
    [:div.p-3.rounded-md
     {:class color-class}
     [:div content]
     [:div.flex.justify-between
      [:button {:on-click #(rf/dispatch [:update-chord-count content :dec])} "-"]
      [:div.px-5 (str " " chord-count " ")]
      [:button {:on-click #(rf/dispatch [:update-chord-count content :inc])} "+"]]]))

(defn chord-grid []
  (let [chord-type @(rf/subscribe [:chord-type])
        chord-list (if (= chord-type "jazz") m/chords m/triads)]
    [:div
     [:div.flex.flex-wrap.gap-3
      (for [chord chord-list]
        ^{:key chord}
        [chord-button chord])]
     [:div.flex.flex-row-reverse.gap-4.mt-6.mb-16
      [:button.p-3.rounded-md.bg-blue-100
       {:on-click #(rf/dispatch [:clear-selected])}
       "Clear All"]
      [:button.p-3.rounded-md.bg-blue-400
       {:on-click #(rf/dispatch [:select-all])}
       "Select All"]]]))

(defn practice-section []
  (let [active-chord @(rf/subscribe [:active-chord])
        tempo @(rf/subscribe [:tempo])
        met-active @(rf/subscribe [:metronome-active])]
    [:div
     [:div.text-5xl.mx-auto.my-5 (first active-chord)]
     [:div.mb-3 [:span.mr-4 "Next: "] (second active-chord)]
     [:button.p-3.rounded-md.bg-lime-400.next-chord-button
      {:on-click #(rf/dispatch [:cycle-active-chord])}
      "Next"]
     [:div.my-5
      [:input {:type "range"
               :min 40
               :max 120
               :value tempo
               :on-change #(rf/dispatch [:update-tempo (.. % -target -value)])}]
      [:input.m-5.bpm-input {:type "text"
                             :on-change #(rf/dispatch [:update-tempo (.. % -target -value)])
                             :value tempo}]
      (if met-active
        [:button.metronome-button.rounded-md.p-3.bg-red-400 {:on-click #(rf/dispatch [:click-metronome])}  "Stop Metronome"]
        [:button.metronome-button.rounded-md.p-3.bg-green-400 {:on-click #(rf/dispatch [:click-metronome])}  "Start Metronome"])]]))

(defn main-page []
  [:div.max-w-6xl.m-auto.px-4
   [practice-section]
   [:div.my-10]
   [chord-slider]
   [chord-grid]])

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (rdom/render [main-page] (js/document.getElementById "root")))

(defn ^:export ^:dev/once init []
  (rf/dispatch-sync [:init-local-storage])
  (rf/dispatch-sync [:cycle-active-chord])
  (rf/dispatch-sync [:cycle-active-chord])
  (mount-root))
