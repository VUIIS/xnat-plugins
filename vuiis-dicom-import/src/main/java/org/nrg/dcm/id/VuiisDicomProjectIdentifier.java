package org.nrg.dcm.id;

import java.util.Map;
import java.util.SortedSet;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.services.StudyRoutingService;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSortedSet;

public class VuiisDicomProjectIdentifier implements DicomProjectIdentifier {
    public VuiisDicomProjectIdentifier() {
        System.out.println("VuiisDicomProjectIdentifier");
    }

    private static final ImmutableSortedSet<Integer> tags = ImmutableSortedSet.of();

    @Override
    public SortedSet<Integer> getTags() { return tags; }

    @Override
    public XnatProjectdata apply(final UserI user, final DicomObject o) {
        String vuiis_id = null;
        int uindex = -1;
        int cindex = -1;
        String project = null;

        vuiis_id = o.getString(Tag.PatientName);
        if (vuiis_id.indexOf('^') > -1) { // handle DICOM from VUIIS OCT
            cindex = vuiis_id.indexOf('^');
            project = vuiis_id.substring(0, cindex).toUpperCase();
        } else { // handle DICOM from VUIIS MR
            uindex = vuiis_id.indexOf('_');
            if (uindex <= 0) { // No split token found or Text only on right
                project = "UNKNOWN_PROJECT";
            } else { // Text on both sides
                project = vuiis_id.substring(0, uindex).toUpperCase();
            }
        }

        return XnatProjectdata.getProjectByIDorAlias(project, user, false);
    }

    @Override
    public void reset() {
        // Nothing to do here since this is just set at initialization.
    }
}
