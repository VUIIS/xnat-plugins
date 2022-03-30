
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.framework.annotations.XnatDataModel;

@XnatPlugin(value = "subjGenProcDataXnatPlugin", name = "DAX subjGenProcData XNAT Plugin",
            description = "This is the DAX subjGenProcData data type for XNAT Plugin.",
            dataModels = {@XnatDataModel(value = "proc:subjGenProcData", singular = "Subject Processing", plural="Subject Processings", code = "Subj Proc")})
public class subjGenProcDataXnatPlugin {
}
