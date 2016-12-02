# RecyclerCardView
A vertical/horizonal list with one item on center and others evenly distributed up and down.

# Demo
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
  compile 'com.vivifarm.view:RecyclerCardView:1.2.0'
}
```

## Usage
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