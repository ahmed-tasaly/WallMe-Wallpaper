
<div align="center">

# Wallme-Wallpaper

<a>
<kdb>

<img src="./Images/Wallme_Wallpaper-Logo-Large.png" width=200 style="border-radius:20%"/>

</kdb>

</a>

</div>



<span style="color:#1be44a" > 


[<h2 align="center">Download</h2>](https://github.com/Alaory/WallMe-Wallpaper/releases)


[<h4 align="center">Support me </h4>](https://www.patreon.com/Alaory)

------------------------------------
</br></br></br>

## Note: 

    this is the first time i use kotlin for something
    and also my first released android app :) 
    soooo expect a loooooooot of bugs

</br>


### what is this ?

</span>
</br>

wallme wallpaper is a wallpaper app aimed at simplicity and low peformance. it can gather images from diffrent sources such as reddit and wallhaven and put them in front of you without any hassle 

</br></br>

<span style="color:#1be44a" > 

### why i am making it ?

</span>


</br>
well i've used a lot of wallpaper apps only a few of them meet my requirements. so i decided to make one to what i content 

</br>
</br>
</br>
<span style="color:#1be44a" > 

### why should i use it 

</span>
</br>

you don't need this app, it is aimed at people who realy want to feel comfortable with using their phones or the one who wants a new feel for their phone


</br>
</br>

<span style="color:#1be44a" > 

### what does it offer

</span>
</br>

as now of development nothing much. however check the todo list to see the comming features

</br>
<span style="color:#1be44a" > 

###  how does the app look like



</span> 
</br>
</br>

<div align=center>

<img src="./Images/phone.jpg" alt="drawing" width=200/>

</div>

</br>

it needs a lot of work

</br>
</br>


<span style="color:#50e41b" > 

## TODO
</span>

- [x] add reddit support 
- [x] add reddit filter settings
- [x] add wallhaven support
- [ ] add wallhaven filter settings 
- [ ] add landscape mode for tablets
- [ ] add verical mode for phones
- [ ] add favtorite page
- [ ] add wallhaven sub page
- [ ] add prograss bar when downloading an image
- [ ] add auto wallpaper changer with preferred croping
- [ ] add more ui I SAID MORE
- [ ] more plans comming
- [ ] go to sleep



### whats on my mind now for the app

```mermaid
graph TD;
    MainActivity --> Reddit
    MainActivity --> WallHaven
    MainActivity --> Settings
    MainActivity --> favorite_posts --> Post
    Reddit --> show_subreddits_posts --> Post
    Reddit --> reddit_settings --> Post_preview_quality
    reddit_settings --> Subreddits_names
    Settings --> where_ToSave_images_path
    Settings --> auto_wallpaper_changer --> Time
    Settings --> clear_cache
    WallHaven --> main_page --> users_posts --> Post
    main_page --> search_tags 
    WallHaven --> wallhaven_settings --> subscription_page_users --> users_posts
```



### librays
    - coil 
    - okhttp
    - TouchImageView



