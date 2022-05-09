
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.framework.annotations.XnatDataModel;

@XnatPlugin(value = "subjGenProcDataXnatPlugin", name = "DAX subjGenProcData XNAT Plugin",
            description = "This is the DAX subjGenProcData data type for XNAT Plugin.",
            dataModels = {@XnatDataModel(value = "proc:subjGenProcData", singular = "SubjectProcessing", plural="SubjectProcessings", code = "SubjProc")})
public class subjGenProcDataXnatPlugin {
}
