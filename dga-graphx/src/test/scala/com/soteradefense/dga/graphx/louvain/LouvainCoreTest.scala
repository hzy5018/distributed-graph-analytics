/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.soteradefense.dga.graphx.louvain

import junit.framework.TestCase
import org.apache.spark.graphx.{Edge, Graph}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.{After, Before, Test}

class LouvainCoreTest extends TestCase {

  var sc: SparkContext = null

  @Before
  override def setUp() {
    val conf = new SparkConf().setMaster("local").setAppName(this.getName)
    sc = new SparkContext(conf)
  }

  @Test
  def testLouvainOneCommunity() {
    val data = Array("1,2,1", "2,3,1", "3,4,1", "4,5,1", "5,6,1")
    val rdd = sc.parallelize(data.toSeq)
    val edgeRDD: RDD[Edge[Long]] = rdd.map(f => {
      val tokens = f.split(",")
      new Edge(tokens(0).toLong, tokens(1).toLong, tokens(2).toLong)
    })
    val graph = Graph.fromEdges(edgeRDD, None)
    val runner = new LouvainTestRunner(2000, 1)
    val result = runner.run(sc, graph)
    assert(result.vertices.map(m => m._2.community == 1).reduce((a, b) => a == b))
  }

  @Test
  def testLouvainMultipleCommunities() {
    val data = Array("1,2,1", "2,3,1", "3,4,1", "4,5,1", "5,6,1", "10,16,1", "10,11,1", "10,12,1", "10,13,1", "10,14,1", "10,15,1")
    val rdd = sc.parallelize(data.toSeq)
    val edgeRDD: RDD[Edge[Long]] = rdd.map(f => {
      val tokens = f.split(",")
      new Edge(tokens(0).toLong, tokens(1).toLong, tokens(2).toLong)
    })
    val graph = Graph.fromEdges(edgeRDD, None)
    val runner = new LouvainTestRunner(2000, 1)
    val result = runner.run(sc, graph)
    assert(result.vertices.filter(f => f._2.community == 1).count() == 2)
    assert(result.vertices.filter(f => f._2.community == 4).count() == 2)
    assert(result.vertices.filter(f => f._2.community == 5).count() == 2)
    assert(result.vertices.filter(f => f._2.community == 10).count() == 7)
  }

  @Test
  def testLouvainMultipleComponents(): Unit = {
    val data = Array(
      "1,2,1", "2,3,1", "3,4,1", "4,5,1", "5,6,1", "25,6,1", "880,25,1", "25,880,1", "880,6,1", "6,25,1", "6,880,1",
      "15,24,1", "655,24,1", "900,655,1", "400,15,1", "900,33,1",
      "7,8,1", "8,9,1", "0,198,1", "435,44,1", "9,0,1", "44,8,1", "7,9,1", "8,7,1", "9,7,1", "9,8,1",
      "10,11,1", "11,12,1", "12,10,1", "10,13,1", "13,14,1", "11,10,1", "10,12,1", "12,11,1")
    val rdd = sc.parallelize(data.toSeq)
    val edgeRDD: RDD[Edge[Long]] = rdd.map(f => {
      val tokens = f.split(",")
      new Edge(tokens(0).toLong, tokens(1).toLong, tokens(2).toLong)
    })
    val graph = Graph.fromEdges(edgeRDD, None)
    val runner = new LouvainTestRunner(2000, 1)
    val result = runner.run(sc, graph)
    assert(result.vertices.filter(f => f._2.community == 13).count() == 2)
    assert(result.vertices.filter(f => f._2.community == 15).count() == 2)
    assert(result.vertices.filter(f => f._2.community == 4).count() == 2)
    assert(result.vertices.filter(f => f._2.community == 25).count() == 3)
    assert(result.vertices.filter(f => f._2.community == 11).count() == 3)
    assert(result.vertices.filter(f => f._2.community == 0).count() == 2)
    assert(result.vertices.filter(f => f._2.community == 655).count() == 2)
    assert(result.vertices.filter(f => f._2.community == 1).count() == 3)
    assert(result.vertices.filter(f => f._2.community == 33).count() == 2)
    assert(result.vertices.filter(f => f._2.community == 7).count() == 3)
    assert(result.vertices.filter(f => f._2.community == 44).count() == 2)
  }

  @After
  override def tearDown() {
    sc.stop()
  }

}
