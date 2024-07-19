package speck.staticfiles;


import java.nio.file.Paths;

import static speck.utils.StringUtils.removeLeadingAndTrailingSlashesFrom;

/**
 * Created by Per Wendel on 2016-11-05.
 */
public class StaticFilesFolder {
    private static final System.Logger LOG = System.getLogger(StaticFilesFolder.class.getName());
    
    private static volatile String local;
    private static volatile String external;

    @Deprecated
    public static final void localConfiguredTo(String folder) {

        local = removeLeadingAndTrailingSlashesFrom(folder);
    }

    @Deprecated
    public static final void externalConfiguredTo(String folder) {

        String unixLikeFolder = Paths.get(folder).toAbsolutePath().toString().replace("\\", "/");
        LOG.log(System.Logger.Level.WARNING, "Registering external static files folder [{0}] as [{1}].", folder, unixLikeFolder);
        external = removeLeadingAndTrailingSlashesFrom(unixLikeFolder);
    }

    @Deprecated
    public static final String local() {
        return local;
    }

    @Deprecated
    public static final String external() {
        return external;
    }

}
