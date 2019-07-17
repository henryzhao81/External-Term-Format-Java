We are using Riak(https://riak.com/products/) to store cookie sync data in database, to save storage size, data stored in Riak is using Erlang external term format (http://erlang.org/doc/apps/erts/erl_ext_dist.html#id101182) which similar to Protobuf, erlang language provide API to encoding/decoding this format, but for other language, looks lack support for this data format. For other backend service which written in different language like Java, Go etc to access database by using ETF format directly to save disk space, different language support to encoding/decoding is neccessary.

In our case, 2 months of cookie sync data stored in database(Riak) is around 27T, to migrate to use other service written in Java or Go either waiting for 2 months until all data expired is not good choice, this repository is Java Version of Erlang ETF converter.

Erlang external term format encoding/decoding for Java.

For other language support:

GoLang: https://github.corp.openx.com/henry-zhao/uds_http

In Erlang the BIF erlang:term_to_binary/1,2 is used to convert a term into the external format. To convert binary data encoding to a term, the BIF erlang:binary_to_term/1 is used.

Based on description of ETF format: http://erlang.org/doc/apps/erts/erl_ext_dist.html#id101182, following are the example of the implementation of term_to_binary and binary_to_term by Java.

To allow other application access those data without using erlang, following are the example of how to encoding and decoding, we are using Riak Java Client for demonstration to store and retrieve data from riak which currently all data are stored in ETF, for riak java client please refer to: https://github.com/basho/riak-java-client

Before start, add some testing data by using uds client, in erlang remote shell:

> AdvId = list_to_binary (["adv_","2221"]).
<<"adv_2221">>

> AdvRtbData = list_to_binary (["rtb_data_","2221"]). 
<<"rtb_data_2221">>

> ds_user_client:set_advertiser_rtb("68d09203-21ad-4dd0-2221-de689e61dffc", AdvId, AdvRtbData, dict:new()).
ok

Verify data in remote shell: 

>dsu_server_riak_client:get_advertiser_data("68d09203-21ad-4dd0-2221-de689e61dffc").

{{dsu_server_adv_obj,[{{<<"adv_2221">>,<<"rtb_data_2221">>},
                       1520977140346460}],
                     [],[],undefined},

Testing data stored into riak.


Now start Testing using Java client to retrieve riak data which stored by uds client:

      //Create Riak Client
      RiakNode.Builder builder = new RiakNode.Builder();
      builder.withMinConnections(10);
      builder.withMaxConnections(50);
      
      List<String> addresses = new LinkedList<String>();
      addresses.add("qa-database-riak-userdata-xv-02.xv.dc.openx.org"); //Using QA Riak for testing
      
      List<RiakNode> nodes = RiakNode.Builder.buildNodes(builder, addresses);
      RiakCluster cluster = new RiakCluster.Builder(nodes).build();
      cluster.start();
      RiakClient client = new RiakClient(cluster);
      
      Namespace ns = new Namespace("a"); //Advertiser Bucket

      //Retrieve Data from Riak
      byte[] newBytes = ETF.string_to_uuid("68d09203-21ad-4dd0-2221-de689e61dffc"); // erlang uuid:string_to_uuid
      Location location = new Location(ns, BinaryValue.create(newBytes));
      FetchValue fv = new FetchValue.Builder(location).build();
      FetchValue.Response response = client.execute(fv);
      RiakObject obj = response.getValue(RiakObject.class);
      if(obj != null) {
        Object object = ETF.binary_to_term(obj.getValue().getValue()); // Decoding, convert binary data to Tuple and List
        ...


      //Store New Data to Riak
      //Construct Data structure
      Object[] strLev1 = new Object[]{"adv_2227".getBytes(), "rtb_data_2227".getBytes()};
      Tuple level1 = new Tuple(strLev1);
      Object[] objLev2 = new Object[]{level1, BigInteger.valueOf(1520637005237699L)};
      Tuple level2 = new Tuple(objLev2);
      
      Object[] strLev11 = new Object[]{"adv_2228".getBytes(), "rtb_data_2228".getBytes()};
      Tuple level11 = new Tuple(strLev11);
      Object[] objLev22 = new Object[]{level11, BigInteger.valueOf(1520637005237690L)};
      Tuple level22 = new Tuple(objLev22);

      ErlangList lev3 = new ErlangList(new Object[]{level2}, level22);

      Object[] lev4 = new Object[]{lev3, null, null};
      Tuple level3 = new Tuple(lev4);

      //convert to binary
      byte[] newVal = ETF.term_to_binary(level3);// Encoding, convert Tuple and List to binary and store to Riak

      //store
      byte[] newkey = ETF.string_to_uuid("68d09203-21ad-4dd0-2227-de689e61dffc"); // erlang uuid:string_to_uuid
      RiakObject riakObject = new RiakObject();
      riakObject.setValue(BinaryValue.create(newVal));
      Location newlocation = new Location(ns, BinaryValue.create(newkey));
      StoreValue store = new StoreValue.Builder(riakObject)
        .withLocation(newlocation)
        .withOption(Option.W, new Quorum(3)).build();
      client.execute(store);


To Verify, we need using erlang remote shell to check if data really store into database correctly

> dsu_server_riak_client:get_advertiser_data("68d09203-21ad-4dd0-2227-de689e61dffc").

{{dsu_server_adv_obj,[{{<<"adv_2227">>,<<"rtb_data_2227">>},
                       1520637005237699},
                      {{<<"adv_2228">>,<<"rtb_data_2228">>},1520637005237690}],
                     [],[],undefined},

Encoding/Decoding works.

All Data stored in Riak is using zlib to compress
