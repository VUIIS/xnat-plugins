
import org.nrg.framework.annotations.XnatPlugin;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@XnatPlugin(value = "vuiis_plugin_vuiisdicomidentifier",
            name = "XNAT 1.7 VUIIS DICOM Identifier Plugin",
            description = "This is the XNAT 1.7 VUIIS DICOM Identifier Plugin.",
            entityPackages = "org.nrg.framework.orm.hibernate.HibernateEntityPackageList",
            log4jPropertiesFile = "vuiis-module-log4j.properties")
@ImportResource("WEB-INF/conf/dicom-import-context.xml")
public class VuiisDicomIdentifierPlugin {
}

