(ns forum.handler-test
  (:use [ring.mock.request]  
        [forum.handler])
  (:require [expectations :refer :all]))

;; Should 404 when forumid or topicid are not numeric (Instead of crash).
(expect 404 (:status (app (request :get "/forums/abc"))))
(expect 404 (:status (app (request :get "/forums/abc/topics/def"))))

;; (deftest test-app
;;   (testing "main route"
;;     (let [response (app (request :get "/"))]
;;       (is (= (:status response) 200))
;;       (is (= (:body response) "Hello World"))))
  
;;   (testing "not-found route"
;;     (let [response (app (request :get "/invalid"))]
;;       (is (= (:status response) 404)))))
