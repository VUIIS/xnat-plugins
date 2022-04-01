from dax import XnatUtils
from test_subjgenproc import upload_assessor_subjgenproc

import sys
assr = sys.argv[1]
#assr = 'DepMIND2-x-7500-x-fmri_emostroop_BLvsWK12_v2-x-e339674b'

dirpath = '/Users/boydb1/UPLOADQ/' + assr

# upload subject assessor
#with XnatUtils.get_interface(host='http://localhost') as xnat:
with XnatUtils.get_interface(host='https://xnat2.vanderbilt.edu/xnat') as xnat:
    upload_assessor_subjgenproc(dirpath, xnat, delete=False, plugin_version='1')
