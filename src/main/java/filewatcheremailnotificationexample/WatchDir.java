package filewatcheremailnotificationexample;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;

public class WatchDir {

	private final WatchService watcher;
	private final Map<WatchKey,Path> keys;
	private boolean trace = false;

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>)event;
	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		if (trace) {
			Path prev = keys.get(key);
			if (prev == null) {
				System.out.format("register: %s\n", dir);
			} else {
				if (!dir.equals(prev)) {
					System.out.format("update: %s -> %s\n", prev, dir);
				}
			}
		}
		keys.put(key, dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	private void registerAll(final Path start) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
					throws IOException
			{
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Creates a WatchService and registers the given directory
	 */
	WatchDir(Path dir) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey,Path>();


		System.out.format("Scanning %s ...\n", dir);
		registerAll(dir);
		System.out.println("Done.");


		// enable trace after initial registration
		this.trace = true;
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	void processEvents() {
		WatchEvent.Kind prevKind = null;
		String prevFileName="";
		for (;;) {

			// wait for key to be signaled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			Path dir = keys.get(key);
			if (dir == null) {
				System.err.println("WatchKey not recognized!!");
				continue;
			}


			for (WatchEvent<?> event: key.pollEvents()) {
				String fileChangeInfo="";
				WatchEvent.Kind kind = event.kind();

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);

				// check conditions which imply higher level events and trigger email notification
				// new file added
				if(prevKind==ENTRY_CREATE && kind==ENTRY_MODIFY && prevFileName.equals(name.toString()) && !Files.isDirectory(child)){
					fileChangeInfo="A new document has been added to the following path on Intranet: " + child.toString().substring(2);
					Email.send(fileChangeInfo);
				}
				// file deleted
				if(kind==ENTRY_DELETE && !prevFileName.equals(name.toString()) && !Files.isDirectory(child)){
					fileChangeInfo="A document has been deleted from the following path on Intranet: " + child.toString().substring(2);
					Email.send(fileChangeInfo);
				}
				// file modified (replaced)
				if(kind==ENTRY_MODIFY && !prevFileName.equals(name.toString()) && !Files.isDirectory(child)){
					fileChangeInfo="A document has been modified on the following path on Intranet: " + child.toString().substring(2);
					Email.send(fileChangeInfo);
				}

				prevKind = kind;
				prevFileName = name.toString();

				// if directory is created, and watching recursively, then
				// register it and its sub-directories
				if (kind == ENTRY_CREATE) {
					try {
						if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
							registerAll(child);
						}
					} catch (IOException x) {
						x.printStackTrace();
					}
				}
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}

	static void usage() {
		System.err.println("usage: java WatchDir directory_to_watch path_to_contactlist_file");
		System.exit(-1);
	}

	public static void main(String[] args) throws IOException {
		// parse arguments
		if (args.length == 0 || args.length > 2)
			usage();

		// register directory, load contact list and process events
		Path dir = Paths.get(args[0]);
		ContactList.loadListFromFile(args[1]);
		new WatchDir(dir).processEvents();
	}
}
