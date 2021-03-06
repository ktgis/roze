/*
 *  Copyright 2017 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.kt.rozenavi.data;

import androidx.lifecycle.LifecycleOwner;

/**
 * 구독 이후의 이벤트만 받고 싶을 떄 사용.
 * 동일 데이터 상에서 구독 이전의 lastest value 를 알고 싶을 경우
 * 기존의 LiveData 와 동일한 방식으로 observe 하여 해결 할 수 있다.
 */
public class SingleMessage<T> extends SingleLiveEvent<T> {

    public void observe(LifecycleOwner owner, final SingleMessageObserver observer) {
        super.observe(owner, t -> {
            if (t == null) {
                return;
            }
            observer.onNewMessage(t);
        });
    }

    public interface SingleMessageObserver<T> {
        void onNewMessage(T message);
    }

}
