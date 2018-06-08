//
//  BucketRepository.h
//  StorjMobile
//
//  Created by Barterio on 3/23/18.
//  Copyright © 2018 Storj. All rights reserved.
//

@import Foundation;
#import "BaseRepository.h"

#import "BucketDbo.h"
#import "BucketModel.h"
@class Response;

@interface BucketRepository : BaseRepository

-(instancetype) init;

-(NSArray *) getAll;

-(NSArray *) getAllWithOrderByColumn: (NSString *) columnName
                               order:(BOOL) isDescending;

-(NSArray *) getStarred;

-(BucketDbo *) getByBucketId:(NSString *) bucketId;

-(BucketDbo *) getBucketByBucketName: (NSString *) bucketName;

-(BucketDbo *) getByColumnName:(NSString *) columnName
                   columnValue:(NSString *) columnValue;

-(Response *) insertWithModel: (BucketModel *) model;

-(Response *) deleteByModel: (BucketModel *) model;

-(Response *) deleteById: (NSString *) bucketId;

-(Response *) deleteByIds: (NSArray *) bucketIds;

-(Response *) deleteAll;

-(Response *) updateByModel: (BucketModel *) model;

-(Response *) updateById:(NSString *)bucketId
                 starred:(BOOL) isStarred;

@end
