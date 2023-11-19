(ns vamp.events
  (:require
   [re-frame.core :as rf]
   [vamp.music :as m]
   [akiroz.re-frame.storage :refer [persist-db-keys]]))

(defn add-or-remove [coll el]
  (if (contains? coll el)
    (disj coll el)
    (conj coll el)))

(defn get-random-chord [coll el]
  (let [coll-without-element (disj coll el)]
    (rand-nth (seq coll-without-element))))

(defn cycle-random-chord [coll current]
  (let [new-live (second current)
        coll-without-current (disj coll new-live)]
    [new-live (rand-nth (seq coll-without-current))]))

(defn persisted-reg-event-db
  [event-id handler]
  (rf/reg-event-fx
   event-id
   [(persist-db-keys :vamp-app [:selected-chords :tempo])]
   (fn [{:keys [db]} event-vec]
     {:db (handler db event-vec)})))

(persisted-reg-event-db :init-local-storage (fn [db] db))

(persisted-reg-event-db
 :toggle-chord
 (fn [db [_ chord]]
   (let [selected-chords (or (:selected-chords db) #{})
         new-selected-chords (add-or-remove selected-chords chord)]
     (assoc db :selected-chords new-selected-chords))))

(persisted-reg-event-db
 :clear-selected
 (fn [db [_]]
   (assoc db :selected-chords #{})))

(persisted-reg-event-db
 :select-all
 (fn [db [_]]
   (assoc db :selected-chords (set m/chords))))

(rf/reg-event-fx
 :cycle-active-chord
 (fn [{:keys [db]} [_]]
   {:db (assoc db :active-chord (cycle-random-chord (:selected-chords db) (:active-chord db)))}))

(rf/reg-event-fx
 :cycle-active-vamp
 (fn [{:keys [db]} [_]]
   {:db (assoc db :active-vamp (take 4 (shuffle (seq (:selected-chords db)))))}))

(rf/reg-event-fx
 :click-metronome
 (fn [{:keys [db]} [_]]
   (.click (js/$ ".metronome-toggle"))
   {:db (assoc db :metronome-active (not (:metronome-active db)))}))

(persisted-reg-event-db
 :update-tempo
 (fn [db [_ tempo]]
   (assoc db :tempo tempo)))

;; --- subs --- ;;

(rf/reg-sub
 :selected-chords
 (fn [db _]
   (-> db :selected-chords)))

(rf/reg-sub
 :active-chord
 (fn [db _]
   (-> db :active-chord)))

(rf/reg-sub
 :active-vamp
 (fn [db _]
   (-> db :active-vamp)))

(rf/reg-sub
 :tempo
 (fn [db _]
   (-> db :tempo)))

(rf/reg-sub
 :metronome-active
 (fn [db _]
   (-> db :metronome-active)))

(comment

  ,)
