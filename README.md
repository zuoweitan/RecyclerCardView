# RecyclerCardView & PaddingLayoutManager
both to support auto animate a item to center.

# PaddingLayoutManager
A vertical LayoutManager only layout specified items with the some Gap.

### Demo
![](https://github.com/zuoweitan/RecyclerCardView/blob/master/art/p1.gif)
![](https://github.com/zuoweitan/RecyclerCardView/blob/master/art/p2.gif)
![](https://github.com/zuoweitan/RecyclerCardView/blob/master/art/p3.gif)

# RecyclerCardView
A vertical/horizonal list with one item on center and others evenly distributed up and down.

### Vertical
![](https://github.com/zuoweitan/RecyclerCardView/blob/master/art/demo_1.gif)

### Horizonal
![](https://github.com/zuoweitan/RecyclerCardView/blob/master/art/demo_2.gif)

# How do I use it?

## Setup

#### Gradle

```groovy
dependencies {
  // jCenter
  compile 'com.vivifarm.view:RecyclerCardView:1.4.0'
}
```

## Usage

### demo
```java
boolean vImg = false;//true to vertical demo

recyclerView = (RecyclerCardView) findViewById(R.id.recyclerView);
final CardLinearLayoutManager linearLayoutManager = new CardLinearLayoutManager(this,
        vImg? RecyclerCardView.VERTICAL : RecyclerView.HORIZONTAL,false);
recyclerView.setLayoutManager(linearLayoutManager);

SampleCardAdapter sampleCardAdapter = new SampleCardAdapter(recyclerView,3,vImg ? 0:60)
        .setvImg(vImg)
        .setList(list);
recyclerView.setAdapter(sampleCardAdapter);

cardSnapHelper = new LinearSnapHelper();
cardSnapHelper.attachToRecyclerView(recyclerView);
```

### CardAdapter

#### Constructor
```java
public CardAdapter(RecyclerView recyclerView,int visibleCount,int minNap){
   ...
}
```
**note**:

1. visibleCount: number of visible, not support value 2.
2. minNap : min value of both padding.(left and right or top and bottom).


### CardHolder
```java
class SimpleCardHolder extends RecyclerCardView.CardHolder<CardItem> implements RecyclerCardView.OnCenterProximityListener{
    @Override
    public void onCenterPosition(boolean animate) {
        ...
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        ...
    }
}
```

**note**:

* your CardHolder can implement OnCenterProximityListener and do you animation on onCenterPosition,onNonCenterPosition.
    

