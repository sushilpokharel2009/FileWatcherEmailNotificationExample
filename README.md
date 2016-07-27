# FileWatcherEmailNotificationExample

The idea behind this code example was to automate sending of email notifications to intranet bulletin board users. A file watcher service was implemented to keep track on file changes within intranet directory on server filesystem, so each new file change (addition, deletion, modification) triggers a new email notification to users.

Usage: 
the code should be built as executable jar file, and it should be run with following two arguments: directory_to_watch path_to_contactlist_file. Contact list file should contain emails separated by a comma (,).
