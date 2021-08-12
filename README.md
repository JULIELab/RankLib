# RankLib, JULIE Lab Version

This repository contains a close copy of the RankLib code of the Lemur project as checked out from the SVN (https://sourceforge.net/p/lemur/code/HEAD/tree/).
The first reason for this copy is to create a Maven artifact that is placed in Maven Central.
The second reason are small changes to adapt the project to our use cases. Specifically, the original RankLib tool is meant to be used on a single task within one JVM due to the extensive use of application global, static fields. We would like to be able to use different data with different algorithms in the same VM, however. Thus, we replaced or complemented the static fields with object-private fields for thread safety and the ability to use multiple LtR models within the same VM.

## How to update to new RankLib releases

The Lemur project uses the SourceForge SVN version control system while we use Git(Hub). While the RankLib SVN code can be checkout out with history via `git svn clone https://svn.code.sf.net/p/lemur/code/RankLib/trunk`, this creates a new Git working tree that is incompatible with this repository. A simple merge is, thus, not possible (or at least I don't know how).
The easiest workflow that I've found is the following:
* Connect this repository with the Lemur RankLib SVN.
* Use `git difftool` to check changes and adapt them to not override our changes from this repository. We use the `-d` option to store the new files - including the subdirectories - to a temporary folder.
* Copy the contents of the temporary folder into this repository to apply the changes.

### Connect to Lemur SVN

Edit the `.git/config` file of this repository and add
```
[svn-remote "lemur"]
	url = https://svn.code.sf.net/p/lemur/code/RankLib/trunk
	fetch = :refs/remotes/git-svn
```
This creates a new remote repository which is the SVN of the Lemur Project.

### Create the files with the new changes but without overriding our changes

This is the tricky part. Via the command

`git difftool -t 'opendiff' -d master remotes/lemur/master`

a tool opens (at least on my Big Sur Mac) that shows all files that are different to the remote. The changes are accepted or discarded via the chosen merge tool (here: opendiff). The tool will request a directory where to save the files in their final state.
The issue here is, of course, to know which changes adapt and which to discard.
The changes made are mostly regarding static fields, most prominently the `DataPoint#featureCount` field which is missing in JULIE Lab's version. The compensate for that, the number of features is counted from the data in a few locations to obtain the `knownFeatures` value.
Also, some LtR algorithms might have their parameters copied in object-private fields. Appropriate getters and setters or other means of configuration methods might have been added.

### Apply the changes to this repository

Finally, just copy the files from the temporary directory recursively back into this repository. If the merging process went alright, everything should be in order now.
At time of writing this, all tests are either running or have deliberately been ignored. So broken tests would definitively point to some mistake, if the tests in the Lemur version also work.