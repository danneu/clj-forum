# clj-forum

[Live Demo](http://198.58.119.43:3000/) (Running in development mode.)

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
