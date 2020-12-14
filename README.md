# Auction Application
This is the server side of an auction application based on Spring boot that we created it for Rahnema College Internship summer 2019.

This application serves some REST APIs services and push notification (WebSocket) to the clients. It uses a relational database (MariaDB) to store the data.

## Explore Rest APIs
```
GET /auctions/category
GET /auctions/find/{id}
POST /auctions/add/picture/{id}
POST /auctions/add
POST /auctions/addBookmark
```
```
POST /users/login
POST /users/signup
POST /users/edit
POST /users/edit/picture
POST /users/edit/password
GET /users/me
```
```
POST /home/search/{category}
GET /home/hottest
```
```
POST /forgot
GET /reset
POT /reset
```
