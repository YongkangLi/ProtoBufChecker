import os
import sys
import subprocess
apps = ['hbase', 'hdfs', 'mesos', 'yarn', 'hive']
if len(sys.argv) > 1:
  folder = sys.argv[1]
else:
  folder = "log/"
s1, s2 = 0, 0
print("APP", "ERROR", "WARNING")
for app in apps:
  rs = []
  for t in ['ERROR', 'WARNING', 'ENUMPROBLEM']:
    search_key = t
    search_app = app
    if app == 'hdfs' or app == 'yarn':
      search_key = "{}.*{}".format(t, app)
      search_app = "hadoop"
    cmd = "grep {} {}/*{}* | wc -l".format(search_key, folder, search_app)
    output = subprocess.check_output(cmd, shell=True)
    v = output.split()[0]
    rs.append(int(v.decode("utf-8")))
  v1 = rs[0]
  if app not in ['accumulo', 'impala']:
    v2 = rs[1] + rs[2]
  else:
    v2 = rs[1]
  s1 += v1
  s2 += v2
  print(app, v1,  v2)
print("SUM", s1, s2 )
