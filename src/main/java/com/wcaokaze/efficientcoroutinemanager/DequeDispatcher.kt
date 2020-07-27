/*
 * Copyright 2020 wcaokaze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wcaokaze.efficientcoroutinemanager

import kotlinx.coroutines.*
import java.util.concurrent.*
import kotlin.concurrent.*
import kotlin.coroutines.*

/**
 * タスクを両端キューに追加するDispatcherです。
 *
 * [first]で両端キューの先頭に、[last]で両端キューの末尾にタスクを追加でき、
 * タスクは先頭から順に実行されます。
 *
 * @param workerThreadCount
 *   両端キューに追加されたタスクを実行するためのスレッドの数
 */
class DequeDispatcher(workerThreadCount: Int = 3) {
   private val deque = LinkedBlockingDeque<Runnable>()

   init {
      repeat (workerThreadCount) {
         thread {
            while (true) {
               try {
                  // clear interruption status
                  Thread.interrupted()

                  deque.takeFirst().run()
               } catch (e: Exception) {
                  // ignore
               }
            }
         }
      }
   }

   val first: CoroutineDispatcher = object : CoroutineDispatcher() {
      override fun dispatch(context: CoroutineContext, block: Runnable) {
         deque.addFirst(block)
      }
   }

   val last: CoroutineDispatcher = object : CoroutineDispatcher() {
      override fun dispatch(context: CoroutineContext, block: Runnable) {
         deque.addLast(block)
      }
   }
}
