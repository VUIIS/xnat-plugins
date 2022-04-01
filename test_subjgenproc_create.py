from dax import XnatUtils
from test_subjgenproc import create_subjgenproc

proj = 'DepMIND2'
subj = '7500'
proctype = 'fmri_emostroop_BLvsWK12_v2'
procversion = '2.0.0'
assrtemp = '/projects/{}/subjects/{}/experiments/{}/assessors/{}'
assr_bl00 = 'DepMIND2-x-7500-x-7500a-x-fmri_emostroop_v2-x-8f955ed8'
assr_wk12 = 'DepMIND2-x-7500-x-7500c-x-fmri_emostroop_v2-x-fb748b11'
inputs = {
    'assr_bl00': assrtemp.format(proj, subj, '7500a', assr_bl00),
    'assr_wk12': assrtemp.format(proj, subj, '7500c', assr_wk12)}

# xnat = XnatUtils.get_interface(host='http://localhost')
xnat = XnatUtils.get_interface(host='https://xnat2.vanderbilt.edu/xnat')

sassr = create_subjgenproc(
    xnat, proj, subj, proctype, procversion, inputs, plugin_version='1')

print(sassr.label())
