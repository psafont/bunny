#!/bin/bash
virtualenv -p $(which python2) env/testenv
source env/testenv/bin/activate
pip install -U pip setuptools wheel
pip install pyopenssl ndg-httpsclient pyasn1
pip install -e git+https://github.com/common-workflow-language/cwltest.git@master#egg=cwltest
cwltest --test conformance_test_draft-3.yaml --tool ${buildFileDirPath}/rabix -j 16
