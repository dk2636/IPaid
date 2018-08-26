# IPaid
## Project's initial steps
Initially and empty project was created, the following steps describe how the project was initialised:
1. An empty project created in [GitHub](https://github.com/dk2636/IPaid "IPaid"), IPaid, with just the .gitignore and READMe.md files
1. Clone the repository locally and a new "Develop" branch was created and pushed to the remote,
1. Using Android Studio an empty project was created. The project created in a folder called IPaid-2.
1. All files from the IPaid-2 folder, except the .gitignore files, were moved into the IPaid folder.
1. Add, commit the new files into the Develop branch and push it to the origin.
## Store local data with SQLite

### Contract class
`IPaidContract`

Within the contract class we define the database schema and have a convention for where to find database constants.

It is consisted by:
An **_outer_** class, the `IPaidContract' where String constants are used to define the content provider, these are:
- CONTENT_AUTHORITY,
- BASE_CONTENT_URI and
- PATH_TableName.
Note: To prevent someone from accidentally instantiating the contract class, give it an empty private constructor.

And an **_inner_** the `IPaidEntry implements BaseColumns`  that defines constant values for the IPaid database table and columns.

### DbHelper class
`IPaidDbHelper extends SQLiteOpenHelper`

The class manages the database creation and version management. It is used whenever an access to the database is needed.
The `SQLiteOpenHelper` class:
1.	Create a SQLite database when its first accessed.
2.	Gives you a connection to the database.
3.	Manages updating the database schema if version changes
2.	We need to create constants for database name and version.
3.	Implement the `onCreate()` method, this will be executed when the database is first created.
5.	Implement the `onUpgrade()` method – will be executed when the database schema changes.


### Provider class
`IPaidProvider`

### `AndroidManifest.xml`
``` XML
<provider
     android:name=".data.IPaidProvider" -> associates the authority with the Java class
     android:authorities="uk.co.dk2636.ipaid" -> Content Authority
     android:exported="false" />
```