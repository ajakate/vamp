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

(defn persisted-reg-event-db
  [event-id handler]
  (rf/reg-event-fx
   event-id
   [(persist-db-keys :vamp-app [:selected-chords])]
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
   {:db (assoc db :active-chord (get-random-chord (:selected-chords db) (:active-chord db)))}))

(rf/reg-event-fx
 :cycle-active-vamp
 (fn [{:keys [db]} [_]]
   {:db (assoc db :active-vamp (take 4 (shuffle (seq (:selected-chords db)))))}))


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
