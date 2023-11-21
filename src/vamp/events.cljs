(ns vamp.events
  (:require
   [re-frame.core :as rf]
   [vamp.music :as m]
   [akiroz.re-frame.storage :refer [persist-db-keys]]))

(defn add-or-inc [coll el]
  (let [rest (dissoc coll el)
        current-amount (get coll el)]
    (assoc rest el (+ current-amount 1))))

(defn remove-or-dec [coll el]
  (let [rest (dissoc coll el)
        current-amount (get coll el)
        new-amount (- current-amount 1)]
    (if (< new-amount 1)
      rest
      (assoc rest el new-amount))))

(defn cycle-random-chord [coll current]
  (let [new-live (second current)
        map-without-current (dissoc coll new-live)
        full-seq (reduce
                  (fn [acc [chord count]]
                    (concat acc (take count (repeat chord))))
                  [] (seq map-without-current))]
    (if (empty? full-seq)
      nil
      [new-live (rand-nth full-seq)])))

(defn persisted-reg-event-db
  [event-id handler]
  (rf/reg-event-fx
   event-id
   [(persist-db-keys :vamp-app [:selected-chords :tempo])]
   (fn [{:keys [db]} event-vec]
     {:db (handler db event-vec)})))

(persisted-reg-event-db :init-local-storage (fn [db] db))

(persisted-reg-event-db
 :update-chord-count
 (fn [db [_ chord inc-or-dec]]
   (let [func (if (= inc-or-dec :inc) add-or-inc remove-or-dec)
         selected-chords (or (:selected-chords db) {})
         new-selected-chords (func selected-chords chord)]
     (assoc db :selected-chords new-selected-chords))))

(persisted-reg-event-db
 :clear-selected
 (fn [db [_]]
   (assoc db :selected-chords {})))

(persisted-reg-event-db
 :select-all
 (fn [db [_]]
   (assoc db :selected-chords
          (reduce
           (fn [acc k]
             (assoc acc k 1))
           {} m/chords))))

(rf/reg-event-fx
 :cycle-active-chord
 (fn [{:keys [db]} [_]]
   {:db (assoc db :active-chord (cycle-random-chord (:selected-chords db) (:active-chord db)))}))

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
 :tempo
 (fn [db _]
   (-> db :tempo)))

(rf/reg-sub
 :metronome-active
 (fn [db _]
   (-> db :metronome-active)))

(comment

  ,)
