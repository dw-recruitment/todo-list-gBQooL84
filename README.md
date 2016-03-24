# dworx
Democracy Works application assignment

1. Commits have been tagged with step.n for convenient checkout, eg git checkout step.3
2. It just occcurred to me to copy ~/.lein/profiles.clj into the project root as lein-profiles.clj, but you will need to checkout master to find that.
2. In tag step.1 do-list was the operative directory. Hmmm. But even that seems not to work.
3. tag step.2 looks OK. The directions were to add a headline, so I picked a famous one that may have decided a presidential election. In the dolist directory: lein ring server (same from now on)
4. step.3: lein ring server then goto localhost:3000/about
5. step.4: In the commit with the message "Complete step 4", the file fulfilled the requirements but is not suitable for others to Just Load. So the tag is on a very recent commit where I cleaned that up so you can edit in Emacs, fire up a cider repl, and C-c C-k to create and seed (setp 5.ii actually) the database dolist, perhaps after fussing with the DB definitions to suit your postgres setup.
6. step.5: OK
7. step.6: I seem not to have made a commit with the "Complete step 6" message, but this tag seems to be it. This work followed a day long exploration oh hoplon/castra/javelin/datomic when I could not figure out any of the Clojure dom manipulation libraries to get more graceful reload of page than the clumsy solution here. It gets better.
8. step.7: The commit with the message "Complete step 7" just fulfilled the instructions in re what we see (but reload the page and one sees the DB was never updated). The tag step.7 is the next commit where DB is update.
9. step.8: OK
