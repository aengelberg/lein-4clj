# lein-4clj

[4clojure](http://www.4clojure.com/) is a great site for practicing various datatypes and concepts in Clojure.
However, the code editor on the website is somewhat limited, mostly because it doesn't have a REPL to test out individual parts of your code.
I'm not affiliated with 4clojure in any way, but I've created a plugin for working on 4clojure problems in the comfort of your own IDE.


## Usage

Use this for user-level plugins:

Put `[lein-4clj "0.1.0"]` into the `:plugins` vector of your
`:user` profile, or if you are on Leiningen 1.x do `lein plugin install
lein-4clj 0.1.0-SNAPSHOT`.

Use this for project-level plugins:

Put `[lein-4clj "0.1.0"]` into the `:plugins` vector of your project.clj.

	$ lein four :ns <name>
                :problem <4clojure-problem-number>
                :filename <path>

Will create a namespace with the given ns name (or resort to `problemN` if `:problem` is given),
at the filename given (or automatically inside your project's src path).
The new namespace will have some tools in it, including test cases fetched directly from the 4clojure website
(if `:problem` is given). The problem number can be found in the URL of a 4clojure problem page.

These keywords are mostly optional: at least `:ns` or `:problem` must be provided to name the namespace;
and `:filename` is required if you're not inside a lein project.

The namespace will contain `(def __ ...)`; like in 4clojure, you will prepare a solution that is meant to substitute
for `__` in each test case.
If all the local tests pass, simply copy/paste the body of the `def` into the 4clojure submission box.

Note that 4clojure requires one expression to be passed in as a solution (and no `def`s are allowed),
so if you expand your solution to include helper functions, you'll need to compress them into a `let` before
submitting them. Example:

```clojure
; My local code:
(defn helper [foo]
  (inc foo))

(def __
  (fn [bar]
    (dec (helper bar)))
  )

; What I submit:
(let [helper (fn [foo]
               (inc foo))]
  (fn [bar]
    (dec (helper bar))))
```

I've included `example_problem29.clj`, which demonstrates the process of creating a file and solving the problem.

### Ideas for future versions of the plugin

- Support 4clojure problems with "special restrictions"
- Make a more automated way to group multiple function definitions into one `let`
- Copy the problem description into the file as well?

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
