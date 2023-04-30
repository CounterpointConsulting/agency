package com.c20g.labs.agency.milvus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c20g.labs.agency.config.MilvusConfiguration;

import io.milvus.client.MilvusClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.DescribeCollectionResponse;
import io.milvus.grpc.DescribeIndexResponse;
import io.milvus.grpc.MutationResult;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.DescribeCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.InsertParam.Field;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DescribeIndexParam;
import io.milvus.response.DescCollResponseWrapper;
import io.milvus.response.DescIndexResponseWrapper;

@Service
public class MilvusService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MilvusService.class);

    @Autowired
    private MilvusClient milvusClient;

    @Autowired
    private MilvusConfiguration milvusConfiguration;

    public void createCollection() throws Exception {
        FieldType fieldType1 = FieldType.newBuilder()
            .withName("id")
            .withDescription("chunk identifier")
            .withDataType(DataType.Int64)
            .withPrimaryKey(true)
            .withAutoID(true)
            .build();
        
        FieldType fieldType2 = FieldType.newBuilder()
            .withName("parent_id")
            .withDescription("parent of chunk (complete document)")
            .withDataType(DataType.Int64)
            .build();

        FieldType fieldType3 = FieldType.newBuilder()
            .withName("external_id")
            .withDescription("reference to source of vector data (eg. CMS identifier)")
            .withDataType(DataType.String)
            .build();

        FieldType fieldType4 = FieldType.newBuilder()
            .withName("embeddings")
            .withDescription(null)
            .withDataType(DataType.FloatVector)
            .withDimension(milvusConfiguration.getDimensions())
            .build();
        
        CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
            .withCollectionName(milvusConfiguration.getCollection())
            .withDescription("doc search embeddings")
            .withShardsNum(milvusConfiguration.getShards())
            .addFieldType(fieldType1)
            .addFieldType(fieldType2)
            .addFieldType(fieldType3)
            .addFieldType(fieldType4)
            .build();

        try {
            R<RpcStatus> response = milvusClient.withTimeout(milvusConfiguration.getTimeout(), TimeUnit.MILLISECONDS)
                .createCollection(createCollectionReq);

            if(response.getStatus().equals(R.Status.Success.getCode())) {
                LOGGER.debug("Create collection response: " + response.getData().getMsg());
            }
            else {
                LOGGER.error("Error creating collection: " + response.getData().getMsg());
                if(response.getException() != null) {
                    throw new Exception("Error creating embeddings collection", response.getException());
                }
                else {
                    throw new Exception("Error creating embeddings collection");
                }
            }
        }
        catch(Exception e) {
            throw new Exception("Error creating embeddings collection", e);
        }
    }

    public boolean hasCollection() throws Exception {

        try {
            R<Boolean> response = milvusClient.hasCollection(HasCollectionParam.newBuilder()
                .withCollectionName(milvusConfiguration.getCollection())
                .build());
            if(response.getStatus().equals(R.Status.Success.getCode())) {
                return response.getData();
            }
            else {
                LOGGER.error("Error checking if collection exists: " + response.getMessage());
                if(response.getException() != null) {
                    throw new Exception("Error checking if collection exists", response.getException());
                }
                else {
                    throw new Exception("Error checking if collection exists");
                }
            }
        }
        catch(Exception e) {
            throw new Exception("Error checking if collection exists", e);
        }
    }

    public void loadCollection() throws Exception {
        try {
            R<RpcStatus> response = milvusClient.loadCollection(LoadCollectionParam.newBuilder()
                .withCollectionName(milvusConfiguration.getCollection())
                .build());
            if(response.getStatus().equals(R.Status.Success.getCode())) {
                LOGGER.debug("Collection loaded");
            }
            else {
                LOGGER.error("Error loading collection: " + response.getMessage());
                if(response.getException() != null) {
                    throw new Exception("Error loading collection", response.getException());
                }
                else {
                    throw new Exception("Error loading collection");
                }
            }
        }
        catch(Exception e) {
            throw new Exception("Error loading collection", e);
        }
    }

    public DescribeCollectionResponse describeCollection() throws Exception {
        try {
            R<DescribeCollectionResponse> response = milvusClient.describeCollection(DescribeCollectionParam.newBuilder()
                .withCollectionName(milvusConfiguration.getCollection())
                .build());

            if(response.getStatus().equals(R.Status.Success.getCode())) {
                DescCollResponseWrapper wrapper = new DescCollResponseWrapper(response.getData());
                LOGGER.debug("Collection Description\n\n" + wrapper.toString());
                return response.getData();
            }
            else {
                LOGGER.error("Error loading collection: " + response.getMessage());
                if(response.getException() != null) {
                    throw new Exception("Error loading collection", response.getException());
                }
                else {
                    throw new Exception("Error loading collection");
                }
            }
        }
        catch(Exception e) {
            throw new Exception("Error getting collection description", e);
        }
    }

    public void createIndex() throws Exception {
        try {
            R<RpcStatus> response = milvusClient.createIndex(CreateIndexParam.newBuilder()
                .withCollectionName(milvusConfiguration.getCollection())
                .withFieldName("embeddings")
                .withIndexName("idx_embeddings")
                .withIndexType(IndexType.IVF_FLAT)
                .withMetricType(MetricType.L2)
                .withExtraParam("{\"nlist\":128}")
                .withSyncMode(Boolean.TRUE)
                .build());
            if(response.getStatus().equals(R.Status.Success.getCode())) {
                LOGGER.debug("Index created");
            }
            else {
                LOGGER.error("Error creating index: " + response.getMessage());
                if(response.getException() != null) {
                    throw new Exception("Error creating index", response.getException());
                }
                else {
                    throw new Exception("Error creating index");
                }
            }
        }
        catch(Exception e) {
            throw new Exception("Error creating index", e);
        }
    }

    public DescribeIndexResponse describeIndex() throws Exception {
        try {
            R<DescribeIndexResponse> response = milvusClient.describeIndex(DescribeIndexParam.newBuilder()
                .withCollectionName(milvusConfiguration.getCollection())
                .withIndexName("idx_embeddings")
                .build());
            if(response.getStatus().equals(R.Status.Success.getCode())) {
                DescIndexResponseWrapper wrapper = new DescIndexResponseWrapper(response.getData());
                LOGGER.debug("Index Description\n\n" + wrapper.toString());
                return response.getData();
            }
            else {
                LOGGER.error("Error getting index: " + response.getMessage());
                if(response.getException() != null) {
                    throw new Exception("Error getting index", response.getException());
                }
                else {
                    throw new Exception("Error getting index");
                }
            }
        }
        catch(Exception e) {
            throw new Exception("Error getting index", e);
        }
    }

    public MutationResult insert(Long parentId, String externalId, List<Double> embeddings) throws Exception {
        try {
            List<Field> fields = new ArrayList<>();
            
            fields.add(new Field("parent_id", Arrays.asList(parentId)));
            fields.add(new Field("external_id", Arrays.asList(externalId)));
            fields.add(new Field("embeddings", embeddings)); 

            R<MutationResult> response = milvusClient.insert(InsertParam.newBuilder()
                .withCollectionName(milvusConfiguration.getCollection())
                .withFields(null)
                .build());
            if(response.getStatus().equals(R.Status.Success.getCode())) {
                LOGGER.debug("Insert successful: " + response.getData().getIDs());
                return response.getData();
            }
            else {
                LOGGER.error("Error inserting data: " + response.getMessage());
                if(response.getException() != null) {
                    throw new Exception("Error inserting data", response.getException());
                }
                else {
                    throw new Exception("Error inserting data");
                }
            }
        }
        catch(Exception e) {
            throw new Exception("Error inserting data", e);
        }
    }

    
}
