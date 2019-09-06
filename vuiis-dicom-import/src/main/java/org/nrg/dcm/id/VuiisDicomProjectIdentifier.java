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

import org.nrg.xnat.services.cache.UserProjectCache;

public class VuiisDicomProjectIdentifier implements DicomProjectIdentifier {
    public VuiisDicomProjectIdentifier() {
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

        // Check that PatientComments has not been set
        if (o.contains(Tag.PatientComments) && 
            o.getString(Tag.PatientComments) != null && 
            !o.getString(Tag.PatientComments).trim().equals("")) {

            _log.debug("Getting Project from PatientComments");

            // Do what ClassicDicomObjectIdentifier does
            Xnat15DicomProjectIdentifier _id = new Xnat15DicomProjectIdentifier(this.userProjectCache);
            return _id.apply(user, o);
        }

        _log.debug("Parsing Project from PatientID");

        vuiis_id = o.getString(Tag.PatientID);
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

    public final void setUserProjectCache(final UserProjectCache userProjectCache) {
        this.userProjectCache = userProjectCache;
    }

    private UserProjectCache userProjectCache = null;
    
    private static final Logger _log = LoggerFactory.getLogger(VuiisDicomProjectIdentifier.class);

}
