package game.scorecounter.storage;

public class StorageFileNotFoundException extends StorageException {

    public StorageFileNotFoundException(String message){
        super(message);
    }
    StorageFileNotFoundException(String message, Throwable cause){

        super(message,cause);
    }
}
