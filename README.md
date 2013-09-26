# clj-forum

[Live Demo](http://198.58.119.43:3000/) (Running in development mode and probably not n*sync with this repo.)

- Database: Datomic
- Templating: Hiccup
- Routing: Compojure

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

## TODO

- Figure out some less tedious flash message abstraction or a better way to shuffle them along as they appear. Put them in master layout?
- Clean up `db/create-db` vs `db/seed-db` vs `d/connect conn` so I don't have to keep switching.
- Clean up db namespace.

## Notes

- `:flash` messages will be maps where keys zip to [Twitter Bootstrap alert styles](http://getbootstrap.com/2.3.2/components.html#alerts): error, success, info, warn.

## Implementation

I'm figuring things out as I go and leaning on Rails familiarities.

My goal, as I progress, is to figure out what abstractions, organization patterns, and strategies do and do not work.

### UID vs. :db/id

I decided to add a `:<entity-name>/uid` attribute to every entity.

Much like the autoincrementing serial PrimaryKey in the relational databases I'm used to working with, I implemented a transaction function that increments the UID of any forum, post, topic, any time one is created.

UIDs are only incremented in the scope of like-entities. If you create one forum, one topic, and one post, then they all have a UID of 1 (`:forum/uid`, `:topic/uid`, `:post/uid`).

#### Benefits of UID:

- URLs have gone from:

        http://localhost:3000/forums/17592186045421/topics/17592186045427
    
    to:

        http://localhost:3000/forums/1/topics/1

- :db/id is internal to Datomic, so it's regenerated any time, for example, the data is restored from a database.

- Can be used to sort entities.
