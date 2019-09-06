package org.nrg.dcm.id;

import com.google.common.collect.ImmutableSortedSet;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.dcm.ChainExtractor;
import org.nrg.dcm.Extractor;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.Labels;

import javax.inject.Provider;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VuiisDicomObjectIdentifier implements DicomObjectIdentifier<XnatProjectdata> {
    public VuiisDicomObjectIdentifier(final DicomProjectIdentifier identifier,
                                          final Iterable<Extractor> subjectExtractors,
                                          final Iterable<Extractor> sessionExtractors,
                                          final Iterable<Extractor> aaExtractors) {
        this(null, identifier, new ChainExtractor(subjectExtractors), new ChainExtractor(sessionExtractors), new ChainExtractor(aaExtractors));
    }

    public VuiisDicomObjectIdentifier(final DicomProjectIdentifier identifier,
                                          final Extractor subjectExtractor,
                                          final Extractor sessionExtractor,
                                          final Extractor aaExtractor) {
        this(null, identifier, subjectExtractor, sessionExtractor, aaExtractor);
    }

    public VuiisDicomObjectIdentifier(final String name,
                                          final DicomProjectIdentifier identifier,
                                          final Iterable<Extractor> subjectExtractors,
                                          final Iterable<Extractor> sessionExtractors,
                                          final Iterable<Extractor> aaExtractors) {
        this(name, identifier, new ChainExtractor(subjectExtractors), new ChainExtractor(sessionExtractors), new ChainExtractor(aaExtractors));
    }

    public VuiisDicomObjectIdentifier(final String name,
                                          final DicomProjectIdentifier identifier,
                                          final Extractor subjectExtractor,
                                          final Extractor sessionExtractor,
                                          final Extractor aaExtractor) {
        _name = StringUtils.defaultIfBlank(name, StringUtils.uncapitalize(getClass().getSimpleName()));
        _identifier = identifier;
        _subjectExtractor = subjectExtractor;
        _sessionExtractor = sessionExtractor;
        _aaExtractor = aaExtractor;
    }

    public String getName() {
        return _name;
    }

     @Override
    public final XnatProjectdata getProject(final DicomObject o) {
        if (null == user && null != userProvider) {
            user = userProvider.get();
        }

        return _identifier.apply(user, o);
    }

    @Override
    public final String getSessionLabel(final DicomObject o) {
        String vuiis_id = null;
        int uindex = -1;
        int cindex = -1;
        String sesssubj = null;
        String label = null;

        // Check that PatientComments has not been set
        if (o.contains(Tag.PatientComments) && 
            o.getString(Tag.PatientComments) != null && 
            !o.getString(Tag.PatientComments).trim().equals("")) {

            _log.debug("Parsing Session from PatientComments");

            label = Labels.toLabelChars(_sessionExtractor.extract(o));
        } else {

            _log.debug("Parsing Session from PatientID");
            vuiis_id = o.getString(Tag.PatientID);
            if (vuiis_id.indexOf('^') > -1) { // handle DICOM from VUIIS OCT
                cindex = vuiis_id.indexOf('^');
                sesssubj = vuiis_id.substring(cindex + 1);
                if (sesssubj.indexOf('_') > -1) {
                    uindex = sesssubj.indexOf('_');
                    label = sesssubj.substring(0, uindex);
                } else {
                    label =  sesssubj;
                }
            } else { // handle DICOM from VUIIS MR
                uindex = vuiis_id.indexOf('_');
                if (uindex == -1) { // No split token found, use the whole value
                    label = vuiis_id;
                } else if(uindex >= vuiis_id.length() - 1) { // Text only on left side
                    label = "UNKNOWN_SESSION";
                } else { // Text on both sides
                    label = vuiis_id.substring(uindex + 1);
                }
            }
        }

        // If we replace PatientID then we lose the project
        //o.putString(Tag.PatientID, o.vrOf(Tag.PatientID), label);
        
        return label;
    }

    @Override
    public final String getSubjectLabel(final DicomObject o) {
        String vuiis_id = null;
        int uindex = -1;
        int cindex = -1;
        String sesssubj = null;
        String label = null;

        // Check that PatientComments has not been set
        if (o.contains(Tag.PatientComments) && 
            o.getString(Tag.PatientComments) != null && 
            !o.getString(Tag.PatientComments).trim().equals("")) {

            _log.debug("Parsing Subject from PatientComments");
            
            label = Labels.toLabelChars(_subjectExtractor.extract(o));
        } else {
            _log.debug("Parsing Subject from PatientName");
            vuiis_id = o.getString(Tag.PatientName);
            if (vuiis_id.indexOf('^') > -1) { // handle DICOM from VUIIS OCT
                cindex = vuiis_id.indexOf('^');
                sesssubj = vuiis_id.substring(cindex + 1);
                if (sesssubj.indexOf('_') > -1) {
                    uindex = sesssubj.indexOf('_');
                    label = sesssubj.substring(uindex + 1);
                } else {
                    label = sesssubj;
                }
            } else { // handle DICOM from VUIIS MR
                uindex = vuiis_id.indexOf('_');
                if (uindex == -1) { // No split token found, use the whole value
                    label = vuiis_id;
                } else if(uindex >= vuiis_id.length() - 1) { // Text only on left side
                    label = "UNKNOWN_SUBJECT";
                } else { // Text on both sides
                    label = vuiis_id.substring(uindex + 1);
                }
            }
        }

        // Replace PatientName with the parsed subject Label
        _log.debug("Setting PatientName to Subject");
        o.putString(Tag.PatientName, o.vrOf(Tag.PatientName), label);

        return label;
    }

    @Override
    public final ImmutableSortedSet<Integer> getTags() {
        if (!_initialized) {
            initialize();
        }
        return ImmutableSortedSet.copyOf(_tags);
    }

    @Override
    public final Boolean requestsAutoarchive(DicomObject o) {
        final String aa = _aaExtractor.extract(o);
        if (null == aa) {
            return null;
        } else if (TRUE.matcher(aa).matches()) {
            return true;
        } else if (FALSE.matcher(aa).matches()) {
            return false;
        } else {
            return null;
        }
    }

    public final void setUser(final UserI user) {
        this.user = user;
    }

    public final void setUserProvider(final Provider<UserI> userProvider) {
        this.userProvider = userProvider;
    }

    private void initialize() {
        ImmutableSortedSet.Builder<Integer> builder = ImmutableSortedSet.naturalOrder();
        builder.addAll(_identifier.getTags());
        builder.addAll(_aaExtractor.getTags());
        builder.addAll(_sessionExtractor.getTags());
        builder.addAll(_subjectExtractor.getTags());
        _tags.addAll(builder.build());
        _initialized = true;
    }

    private static final Pattern TRUE  = Pattern.compile("t(?:rue)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern FALSE = Pattern.compile("f(?:alse)?", Pattern.CASE_INSENSITIVE);
    private static final Logger _log = LoggerFactory.getLogger(VuiisDicomObjectIdentifier.class);

    private UserI           user         = null;
    private Provider<UserI> userProvider = null;


    private final String                 _name;
    private final DicomProjectIdentifier _identifier;
    private final Extractor              _subjectExtractor, _sessionExtractor, _aaExtractor;
    private final SortedSet<Integer> _tags        = new TreeSet<>();
    private       boolean            _initialized = false;
}
