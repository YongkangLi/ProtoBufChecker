import itertools
import os
import sys
hadoop = ['rel/release-2.10.1', 'rel/release-3.1.4', 'rel/release-3.3.0', 'rel/release-3.2.1', 'rel/release-2.9.2']
hadoop.reverse()
tags_dict = {
  'hbase' :['rel/1.4.13', 'rel/1.6.0', 'rel/2.2.6', 'rel/2.3.3','master'],
  'hive' :['rel/release-1.2.2', 'rel/release-2.1.0', 'rel/release-2.1.1', 'rel/release-2.2.0', 'rel/release-2.3.0', 'rel/release-2.3.1', 'rel/release-2.3.2', 'rel/release-2.3.3', 'rel/release-2.3.4', 'rel/release-2.3.5', 'rel/release-2.3.6', 'rel/release-2.3.7', 'rel/release-3.0.0', 'rel/release-3.1.0', 'rel/release-3.1.1', 'rel/release-3.1.2'],
  'hadoop' : ['rel/release-2.6.4', 'rel/release-2.7.2', 'rel/release-2.8.0',  'rel/release-2.9.0',  'rel/release-2.10.0', 'rel/release-3.0.0', 'rel/release-3.1.0', 'rel/release-3.2.0', 'rel/release-3.3.0', '7bc78ab707901a6ba9b3a517f8e30aa08c88fe52'],
  'mesos': ['1.4.3', '1.5.2', '1.7.0', '1.7.3', '1.9.0', '1.10.0', '1.11.0', "master"],
}

if len(sys.argv) > 1:
  key = sys.argv[1]
  tags = tags_dict[key]
  combinations = list(itertools.combinations(tags,2))
  if key == 'hadoop':
    combinations.append(['rel/release-3.1.0', 'rel/release-3.1.1'])
  for t in combinations:
    log_fn = "log/" + key + "_" + ty +   t[0].replace("/","-") + "--" + t[1].replace("/","-") + ".log"
    cmd = "java -jar target/protoBufChecker-1.0-SNAPSHOT.jar -app {} -v1 {} -v2 {} > {}".format(key, t[0], t[1], log_fn)
    print(cmd)
    os.system(cmd)
else:
  for key in tags_dict:
    tags = tags_dict[key]
    combinations = list(itertools.combinations(tags,2))
    if key == 'hadoop':
      combinations.append(['rel/release-3.1.0', 'rel/release-3.1.1'])
    for t in combinations:
      log_fn = "log/" + key + "_proto" + t[0].replace("/","-") + "--" + t[1].replace("/","-") + ".log"
      cmd = "java -jar target/protoBufChecker-1.0-SNAPSHOT.jar -app {} -v1 {} -v2 {} > {}".format(key, t[0], t[1], log_fn)
      print(cmd)
      os.system(cmd)
