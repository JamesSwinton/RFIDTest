package com.zebra.jamesswinton.rfidtestv2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class ReadTagsAdapter extends RecyclerView.Adapter  {

  // Debugging
  private static final String TAG = "UpcProductImagesAdapter";

  // Constants
  private static final int EMPTY_VIEW_TYPE = 0;
  private static final int DATA_VIEW_TYPE = 1;


  // Static Variables


  // Variables
  private Context mContext;
  private List<TagInfo> mTagDataArray;

  /**
   * Constructor
   */

  public ReadTagsAdapter(Context cx, @Nullable List<TagInfo> tagDataArray) {
    this.mContext = cx;
    this.mTagDataArray = tagDataArray;
  }

  /**
   * Recycler View Methods
   */

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    //
    if (viewType == EMPTY_VIEW_TYPE) {
      return new EmptyViewHolder(LayoutInflater.from(
              parent.getContext()).inflate(R.layout.adapter_empty, parent, false));
    } else {
      return new TagDataHolder(LayoutInflater.from(
              parent.getContext()).inflate(R.layout.adapter_read_tag, parent, false));
    }
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
    // Populate view if ViewType is TagDataHolder
    switch (viewHolder.getItemViewType()) {
      case EMPTY_VIEW_TYPE:

        break;
      case DATA_VIEW_TYPE:
        // Cast ViewHolder
        TagDataHolder vh = (TagDataHolder) viewHolder;

        // Init ViewHolder Details
        vh.tagIdentifier.setText(mTagDataArray.get(position).getTagId());
        vh.tagSelected.setChecked(mTagDataArray.get(position).isChecked());
        vh.tagInfo.setText("Retrieving tag data from server...");

        // TODO: Init API Call

        break;
    }
  }

  @Override
  public int getItemCount() {
    return mTagDataArray != null && !mTagDataArray.isEmpty() ? mTagDataArray.size() : 1;
  }

  @Override
  public int getItemViewType(int position) {
    return mTagDataArray != null && !mTagDataArray.isEmpty() ? DATA_VIEW_TYPE : EMPTY_VIEW_TYPE;
  }

  /**
   * View Holders
   */

  private class TagDataHolder extends RecyclerView.ViewHolder {
    // UI Elements
    LinearLayout baseLayout;
    CheckBox tagSelected;
    TextView tagIdentifier;
    TextView tagInfo;

    private TagDataHolder(@NonNull View itemView) {
      super(itemView);
      baseLayout = itemView.findViewById(R.id.base_layout);
      tagSelected = itemView.findViewById(R.id.tag_selected);
      tagIdentifier = itemView.findViewById(R.id.tag_identifier);
      tagInfo = itemView.findViewById(R.id.tag_info);
    }
  }

  private class EmptyViewHolder extends RecyclerView.ViewHolder {
    private EmptyViewHolder(@NonNull View itemView) {
      super(itemView);
    }
  }
}
