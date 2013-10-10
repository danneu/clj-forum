(ns forum.authentication
  (:import (org.jasypt.util.password StrongPasswordEncryptor)))

(defn encrypt [pwd]
  (.encryptPassword (StrongPasswordEncryptor.) pwd))

(defn match? [pwd digest]
  (.checkPassword (StrongPasswordEncryptor.) pwd digest))
