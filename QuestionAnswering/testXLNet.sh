if [ -z $1 ];
    then
        python ./XLNet/run_squad.py  \
        --model_type xlnet \
        --model_name_or_path ./Models/XLNet  \
        --do_eval  \
        --do_lower_case  \
        --train_file ./Data/XLNet/xlnet_train.json  \
        --predict_file ./Data/XLNet/xlnet_pipeline.json \
        --per_gpu_eval_batch_size 1  \
        --output_dir ./Models/XLNet
elif [ $1 == "clean" ];
    then
        python ./XLNet/run_squad.py  \
        --model_type xlnet \
        --model_name_or_path ./Models/XLNet  \
        --do_eval  \
        --do_lower_case  \
        --train_file ./Data/XLNet/xlnet_train.json  \
        --predict_file ./Data/XLNet/xlnet_clean.json \
        --per_gpu_eval_batch_size 1  \
        --output_dir ./Models/XLNet
else
    echo "Undefined parameter. Use 'clean' as \$1 parameter or remove parameters."
fi
