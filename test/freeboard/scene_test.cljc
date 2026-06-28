(ns freeboard.scene-test
  (:require [clojure.test :refer [deftest is testing]]
            [freeboard.board :as b]
            [freeboard.scene :as sc]))

(deftest hex
  (is (= [1.0 0.0 0.0 1.0] (sc/hex->rgba "#ff0000")))
  (is (< (Math/abs (- 0.533 (first (sc/hex->rgba "#888888")))) 0.01))
  (is (= [1.0 1.0 1.0 1.0] (sc/hex->rgba nil))))

(deftest entities
  (let [bd (-> (b/new-board)
               (assoc :freeboard/viewport {:x 0.0 :y 0.0 :zoom 2.0})
               (b/add-item {:item/id "s" :item/kind :sticky :item/x 10 :item/y 20 :item/w 100 :item/h 50 :item/fill "#ff0000"})
               (b/add-item {:item/id "f" :item/kind :frame  :item/x 0 :item/y 0 :item/w 400 :item/h 300}))
        ents (sc/board->entities bd)
        s    (first (filter #(= "s" (:kami/eid %)) ents))]
    (testing "renderable entity uses SDK component attrs + baked screen transform"
      (is (= [20.0 40.0] (vec (take 2 (:transform/translation s)))))  ; world*zoom
      (is (= [200.0 100.0 1.0] (:transform/scale s)))                 ; w*zoom h*zoom
      (is (= "freeboard:quad" (:mesh/asset s)))
      (is (= [1.0 0.0 0.0 1.0] (:material/tint s))))                  ; fill → tint
    (testing "no-fill item falls back to per-kind tint"
      (is (= (:frame sc/kind-tint) (:material/tint (first (filter #(= "f" (:kami/eid %)) ents))))))))

(deftest snapshot
  (let [bd (-> (b/new-board) (b/add-item {:item/kind :sticky :item/x 0 :item/y 0 :item/w 10 :item/h 10}))
        snap (sc/scene-snapshot bd)]
    (is (= 2 (count (:snapshot/assets snap))))                        ; quad mesh + flat material
    (is (some #(:camera/active? %) (:snapshot/entities snap)))        ; active camera present
    (is (= "freeboard:quad" (:asset/id (first (:snapshot/assets snap)))))))
