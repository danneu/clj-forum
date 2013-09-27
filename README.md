# clj-forum

[Live Demo](http://198.58.119.43:3000/) (Running in development mode and probably not n*sync with this repo.)

- Database: Datomic
- Templating: Hiccup
- Routing: Compojure

I'm figuring things out as I go and leaning on Rails familiarities.

My goal as I progress is to figure out which abstractions, organization patterns, and strategies do and do not work.

## Running it

### Start the transactor

- Download latest [Datomic-free](http://downloads.datomic.com/free.html)
- `cd` into it
- Uncomment the dev laptop settings in `config/samples/free-transactor-template.properties`
- Run the transactor in its own terminal:

        bin/transactor -Xmx1g config/samples/free-transactor-template.properties
    
### Start clj-forum

- `git clone git@github.com:danneu/clj-forum.git`
- `cd clj-forum`
- `lein ring server`

## Implementation

The flow:

1. Routes in `forum.handler` send params to a controller in `forum.controllers.*`.
2. A controller make DB calls and (unimplemented:) authorizes access, then passes values to a view in `forum.views.*`.
3. Views are just Hiccup datastructures.

Middleware:

- `forum.middleware.expose-request` binds a dynamic var `req` that any namespace can refer to if they want to see the full Ring request-map. Ideally, `req` will be broken up into more specific functions/vars sort of like this next middleware:
- `forum.middleware.wrap-current-user` binds a dynamic var `current-user` that either contains the DB entity of a user (`{:user/uname "kate", :user/digest "abc", :user/topics [...], :user/posts [...], ...}`) or `nil` if there is no user logged in. It reads this data from the session.

Session:

- `forum.controllers.sessions` is the controller used in the login/logout flow. It creates a session or sets it to nil.
- The session is stored in an encrypted cookie that's signed by an application-unique `:session-secret` that should be defined in `resources/config.edn`. The secret should be >= 16 bytes (minimum necessary for the AES key).

Database attributes:

User

- :user/uname - username
- :user/digest - plaintext salt concatenated with hashed password (then converted to base64)
- :user/topics - refs of the topics the user has created
- :user/posts
- :user/created - when user was created
- :user/uid - unique id of user that's autoincremented on creation. the preferred lookup attribute.

Forum

- :forum/title
- :forum/desc - description
- :forum/topics
- :forum/uid

Topic

- :topic/title
- :topic/posts
- :topic/uid
- :topic/created

The OP's post in a topic is simply the first post (sorted by uid ascending). A topic is just a titled collection of posts.

Post 

- :post/text
- :post/uid
- :post/created
