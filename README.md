# ProtoBufChecker:

## What can ProtoBufChecker do:

### Serialization Library Compatibility Checker
ProtoBufChecker analyzes data syntax defined using standard serialization libraries and detect incompatibility across versions, which can lead to upgrade failures.
It currently focuses on one widely adopted serialization libraries, [Protocol Buffer](https://developers.google.com/protocol-buffers/docs/proto.).

Protocols evolve over time. Developers can update any protocol to meet the program’s need. However, certain rules have to be followed to avoid data-syntax incompatibility across versions. Particularly, the manuals of Protocol Buffer state the following rules:

    (1). Add/delete required field. 

    (2). The tag number of a field has been changed.

    (3). A required field has been changed to non-required. 
    
    (4). Added or deleted an enum with no 0 value.

Violating the first two rules will definitely lead to upgrade failures caused by syntax incompatibility, which will be referred to as `ERROR` by ProtoBufChecker.
Violating the third rule may lead to failures, which will be referred to as `WARNING` by ProtoBufChecker, if the new version generates data that does not contain its no-longer-required data member.
For other type of changes such as changing field type, ProtoBufChecker will output `INFO` level information.

## Build

Prerequisite: install `protobuf` with:

    $brew install protobuf

Check the version of `protobuf` with:

    $protoc --version

Checkout ProtoBufChecker to your local machine with:

    $git clone https://github.com/YongkangLi/ProtoBufChecker.git

Build executable jar with:

    $cd ProtoBufChecker
    $mvn package

## Usage

### Protocol Buffer Checker
1. Prepare the application that you would like to check the consistency on the same machine, suppose its path is `path_app`.

2. Run Jar (with one git repo)

    `java -jar target/ProtoBufChecker-1.0-SNAPSHOT.jar -app path_app -v1 old_version_tag -v2 new_version_tag`

   e.g. check for proto file (you need to `git clone https://github.com/apache/hbase.git` first):

   `java -jar target/ProtoBufChecker-1.0-SNAPSHOT.jar -app hbase -v1 rel/2.2.6 -v2 rel/2.3.3`

3. Run Jar (with two directories) 

    `java -jar target/ProtoBufChecker-1.0-SNAPSHOT.jar -app path_app -v1 old_version_tag -v2 new_version_tag`

   e.g. check for proto file (you need to have two versions of hbase first):

   `java -jar target/ProtoBufChecker-1.0-SNAPSHOT.jar -o hbase-rel-2.2.6 -n hbase-rel-2.3.3`

## About the [Paper](https://dl.acm.org/doi/10.1145/3477132.3483577) Section 6.2.2

1. Checkout the required applications in the DUPChecker/ directory:

   (1). hbase `git clone https://github.com/apache/hbase.git`

   (2). hdfs, yarn `git clone https://github.com/apache/hadoop.git`

   (3). mesos `git clone https://github.com/apache/mesos.git`

   (4). hive `git clone https://github.com/apache/hive.git`

2. Create a log folder:

   ` mkdir log`

3. Run scripts:

   `python3 scripts/run_experiment.py`

   The results will be output to file under log folder with application's name as prefix.

4. Generate Table 6 in the paper:

   `python3 scripts/export.py`

##  Approximate time needed:

1. local machine: macOS 12.1, 64G RAM, 512G disk.
2. time distribution:

   1) install dependencies and download required git repos - 10 min
   2) run experiments - ~20 min

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## LICENSE

[MIT](https://github.com/jwjwyoung/DUPChecker/blob/master/LICENSE)
