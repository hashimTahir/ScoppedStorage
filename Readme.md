# Scoped Storage

There are two type of storages


#### 1-> Internal

#### 2-> External

Every app has its own private directory inside internal storage and is
not accessable to any other application. Upon uninstall files stored
in internal storage are deleted.

Every thing else is considered shared or external storage. Granting read
or write permissions to even to view the content i.e. pdfs, docs, images
or videos, before android 11 would allow the app to access the entire
external storage wheather it is required or not. Upon uninstall files
creted in external storage stay.

### Scopped Storage introduced in android 10, made mandatory in
android 11 and onwards solves these issues. System (Os) keeps track of
what files are created by which apps, and removes
them on uninstalling.

### From android 11 and onwords every app can access its own folder in internal storage and dosent
requires any permission to access it. Furthermore, apps internal storage directory is completely
unaccessable to everyother app including system apps.
### To change files in external/shared directory, as in media files, not owned by the app
accessing it, For this, Now user permision to modify the content is also required,
with "createWriteRequest" or "createDeleteRequest"

### Now items can be sent to trash, which will be deleted after 30 days, instead of deleting them
directly.

### To access the whole external storage new permission MANAGE_EXTERNAL_STOREAGE is introduced,
which is flagged as dangerous permission and requires manual review and special approval
from google's play store.

### Scopped storage improves the device security by limiting the freedom developers have.