# TODO: check that an assessor doesn't already exist with the
# same proctype and inputs
# TODO: should_upload_assessor
# TODO: copy outlog
# TODO: set dax version and dax docker version
# TODO: modify suppdf to not try to show session,
# needs to be fixed in assessor_utils.parse_full_assessor_name


import os
import sys
import shutil
import json
from uuid import uuid4
import logging
from datetime import date

from dax import XnatUtils, dax_tools_utils, task


logging.basicConfig(stream=sys.stdout, level=logging.DEBUG)
LOGGER = logging.getLogger()


def create_subjgenproc(xnat, project, subject, proctype, procversion, inputs, plugin_version='2'):
    serialized_inputs = json.dumps(inputs)
    guidchars = 8  # how many characters in the guid?
    today = str(date.today())

    # Get a unique ID
    count = 0
    max_count = 100
    while count < max_count:
        count += 1
        guid = str(uuid4())

        sassr = xnat.select('/projects/{}/subjects/{}/experiments/{}'.format(
            project, subject, guid))

        if not sassr.exists():
            break

    if count == max_count:
        LOGGER.error('failed to find unique ID, cannot create assessor!')
        return

    # Build the assessor attributes as key/value pairs
    _label = '-x-'.join([project, subject, proctype, guid[:guidchars]])

    if plugin_version == '1':
        kwargs = {
            'label': _label,
            'ID': guid,
            'proc:subjgenprocdata/proctype': proctype,
            'proc:subjgenprocdata/procversion': procversion,
            'proc:subjgenprocdata/procstatus': task.NEED_INPUTS,
            'proc:subjgenprocdata/date': today}
    else:
        kwargs = {
            'label': _label,
            'ID': guid,
            'proc:subjgenprocdata/proctype': proctype,
            'proc:subjgenprocdata/procversion': procversion,
            'proc:subjgenprocdata/inputs': serialized_inputs,
            'proc:subjgenprocdata/procstatus': task.NEED_INPUTS,
            'proc:subjgenprocdata/date': today}

    # Create the assessor
    LOGGER.info('creating subject asssessor:{}'.format(_label))
    sassr.create(experiments='proc:subjgenprocdata', **kwargs)

    # Return the new assessor object
    return sassr


def upload_resource_subjgenproc(sassr, resource, dirpath):
    if resource == 'SNAPSHOTS':
        upload_snapshots_subjgenproc(sassr, dirpath)
    else:
        # Get list of files in the dir
        rfiles_list = os.listdir(dirpath)

        # Handle none, multiple or one file found
        if not rfiles_list:
            LOGGER.warn('No files in {}'.format(dirpath))
        elif len(rfiles_list) > 1 or os.path.isdir(rfiles_list[0]):
            # Upload the whole dir
            XnatUtils.upload_folder_to_obj(
                dirpath,
                sassr.resource(resource),
                resource,
                removeall=True)
        else:
            # One file, just upload it
            fpath = os.path.join(dirpath, rfiles_list[0])
            XnatUtils.upload_file_to_obj(
                fpath,
                sassr.resource(resource),
                removeall=True)


def upload_snapshots_subjgenproc(sassr, dirpath):
    """
    Upload snapshots to an assessor
    :param assessor_obj: pyxnat assessor Eobject
    :param dirpath: local path
    :return: None
    """
    orig = os.path.join(dirpath, dax_tools_utils.SNAPSHOTS_ORIGINAL)
    thumb = os.path.join(dirpath, dax_tools_utils.SNAPSHOTS_PREVIEW)

    if not os.path.isfile(orig):
        LOGGER.warn('original snapshot not found:{}'.format(orig))
    else:
        # Upload the full size image
        sassr.resource('SNAPSHOTS').file(os.path.basename(orig)).put(
            orig,
            orig.split('.')[1].upper(),
            'ORIGINAL',
            overwrite=True,
            params={"event_reason": "DAX upload"})

    if not os.path.isfile(thumb):
        LOGGER.warn('thumbnail snapshot not found:{}'.format(thumb))
    else:
        # Upload the thumbnail image
        sassr.resource('SNAPSHOTS').file(os.path.basename(thumb)).put(
            thumb,
            thumb.split('.')[1].upper(),
            'THUMBNAIL',
            overwrite=True,
            params={"event_reason": "DAX upload"})


def upload_assessor_subjgenproc(dirpath, xnat, delete=True, plugin_version='2'):
    resdir = os.path.dirname(dirpath)
    diskq_dir = os.path.join(resdir, 'DISKQ')

    assr = os.path.basename(dirpath)
    [proj, subj, proctype, guid] = assr.split('-x-')

    # Get the assessor object on XNAT
    sassr = xnat.select('/projects/{}/subjects/{}/experiments/{}'.format(
        proj, subj, assr))

    if not sassr.exists():
        # Create the assessor
        # LOGGER.info('creating subject asssessor'.format(assr))
        # sassr.create(experiments='proc:subjgenprocdata')
        # We will assume the assessor has already been made elsewhere
        # with the inputs field complete as well
        # as proctype, procversion, and date
        LOGGER.info('assessor does not exist, refusing to create')
        return

    LOGGER.info('uploading:{}:{}'.format(sassr.label(), dirpath))

    # Before Upload
    LOGGER.debug('suppdf')
    dax_tools_utils.suppdf(dirpath, sassr)
    LOGGER.debug('generate_snapshots')
    dax_tools_utils.generate_snapshots(dirpath)

    # Upload each resource
    for resource in os.listdir(dirpath):
        resource_path = os.path.join(dirpath, resource)
        if not os.path.isdir(resource_path):
            LOGGER.debug('skipping, not a directory:{}'.format(resource_path))
            continue

        LOGGER.debug('+uploading:{}'.format(resource))
        try:
            upload_resource_subjgenproc(sassr, resource, resource_path)
        except Exception as e:
            _msg = 'upload failed, skipping assessor:{}:{}'.format(
                resource_path, str(e))
            LOGGER.error(_msg)

    # after Upload
    ctask = task.ClusterTask(assr, resdir, diskq_dir)

    # Set on XNAT
    _git_revision = 'null'
    _dax_version = 'null'
    _dax_docker_version = dax_tools_utils.get_dax_docker_version_assessor(
        dirpath) or 'null'
    _status = ctask.get_status() or 'null'
    _jobid = ctask.get_jobid() or 'null'
    _jobnode = ctask.get_jobnode() or 'null'
    _memused = ctask.get_memused() or 'null'
    _walltime = ctask.get_walltime() or 'null'
    _jobstartdate = ctask.get_jobstartdate() or 'null'

    if plugin_version == '1':
        sassr.attrs.mset({
            'proc:subjgenprocdata/procstatus': _status,
            'proc:subjgenprocdata/validation/status': task.NEEDS_QA,
            'proc:subjgenprocdata/jobid': _jobid,
            'proc:subjgenprocdata/jobnode': _jobnode,
            'proc:subjgenprocdata/memused': _memused,
            'proc:subjgenprocdata/walltimeused': _walltime,
            'proc:subjgenprocdata/jobstartdate': _jobstartdate,
        })
    else:
        sassr.attrs.mset({
            'proc:subjgenprocdata/procstatus': _status,
            'proc:subjgenprocdata/validation/status': task.NEEDS_QA,
            'proc:subjgenprocdata/jobid': _jobid,
            'proc:subjgenprocdata/jobnode': _jobnode,
            'proc:subjgenprocdata/memused': _memused,
            'proc:subjgenprocdata/walltimeused': _walltime,
            'proc:subjgenprocdata/dax_docker_version': _dax_docker_version,
            'proc:subjgenprocdata/jobstartdate': _jobstartdate,
            'proc:subjgenprocdata/dax_version': _dax_version,
            'proc:subjgenprocdata/dax_version_hash': _git_revision,
        })

    if delete:
        # Delete the task from diskq
        ctask.delete()

        # Remove the folder
        shutil.rmtree(dirpath)
